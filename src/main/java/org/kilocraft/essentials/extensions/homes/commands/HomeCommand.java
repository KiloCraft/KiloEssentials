package org.kilocraft.essentials.extensions.homes.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.command.IEssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.api.util.schedule.SinglePlayerScheduler;
import org.kilocraft.essentials.chat.StringText;
import org.kilocraft.essentials.config.ConfigObjectReplacerUtil;
import org.kilocraft.essentials.config.ConfigVariableFactory;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.extensions.homes.api.Home;
import org.kilocraft.essentials.extensions.homes.api.UnsafeHomeException;
import org.kilocraft.essentials.user.UserHomeHandler;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.LocationUtil;
import org.kilocraft.essentials.util.commands.CommandUtils;

public class HomeCommand extends EssentialCommand {
    private static final SimpleCommandExceptionType MISSING_DIMENSION = new SimpleCommandExceptionType(new TextComponent("The Dimension this home exists in no longer exists"));

    public HomeCommand() {
        super("home", CommandPermission.HOME_SELF_TP);
        this.withUsage("command.home.usage", "name");
    }

    public static String replaceVariables(final String str, final OnlineUser source, final User target, final Home home) {
        String string = ConfigVariableFactory.replaceUserVariables(str, source);
        string = ConfigVariableFactory.replaceTargetUserVariables(string, target);

        string = new ConfigObjectReplacerUtil("home", string, true)
                .append("name", home.getName())
                .append("size", target.getHomesHandler().getHomes().size())
                .toString();

        return string;
    }

    @Override
    public final void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
        final RequiredArgumentBuilder<CommandSourceStack, String> homeArgument = this.argument("name", StringArgumentType.word())
                .suggests(UserHomeHandler::suggestHomes)
                .executes(ctx -> this.executeSelf(ctx, true));

        final RequiredArgumentBuilder<CommandSourceStack, String> targetArgument = this.getUserArgument("user")
                .requires(src -> this.hasPermission(src, CommandPermission.HOME_OTHERS_TP))
                .executes(this::executeOthers);

        homeArgument.then(targetArgument);
        this.argumentBuilder.executes(ctx -> this.executeSelf(ctx, false));
        this.commandNode.addChild(homeArgument.build());
    }

    private int executeSelf(final CommandContext<CommandSourceStack> ctx, boolean hasInput) throws CommandSyntaxException {
        final ServerPlayer player = ctx.getSource().getPlayerOrException();
        final OnlineUser user = this.getOnlineUser(player);
        final UserHomeHandler homeHandler = user.getHomesHandler();
        final String input = hasInput ? StringArgumentType.getString(ctx, "name") : "home";
        final String name = input.replaceFirst("-confirmed-", "");

        if (!homeHandler.hasHome(name)) {
            user.sendLangMessage("command.home.invalid_home");
            return IEssentialCommand.FAILED;
        }

        Home home = homeHandler.getHome(name);
        if (LocationUtil.isDestinationToClose(user, home.getLocation())) {
            return IEssentialCommand.FAILED;
        }

        if (home.shouldTeleport()) {
            user.sendLangMessage("command.home.invalid_dim", home.getLocation().getDimensionType().toString());
            return IEssentialCommand.FAILED;
        }

        homeHandler.prepareHomeLocation(user, home);

        new SinglePlayerScheduler(user, 1, KiloConfig.main().server().cooldown, () -> {
            try {
                homeHandler.teleportToHome(user, name);
                user.sendLangMessage("command.home.teleport.self", name);
            } catch (UnsafeHomeException e) {
                if (e.getReason() == UserHomeHandler.Reason.MISSING_DIMENSION) {
                    user.sendError(e.getMessage());
                }
            }
        });

        return IEssentialCommand.SUCCESS;
    }

    private int executeOthers(final CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final ServerPlayer player = ctx.getSource().getPlayerOrException();
        final String input = StringArgumentType.getString(ctx, "name");
        final String name = input.replaceFirst("-confirmed-", "");
        final OnlineUser src = this.getOnlineUser(player);
        final String inputName = StringArgumentType.getString(ctx, "user");

        this.getUserManager().getUserThenAcceptAsync(src, inputName, user -> {
            final UserHomeHandler homeHandler = user.getHomesHandler();
            if (!homeHandler.hasHome(name)) {
                src.sendLangMessage("commands.playerHomes.invalid_home");
                return;
            }

            if (homeHandler.getHome(name).shouldTeleport()) {
                src.sendLangMessage("command.home.invalid_dim", homeHandler.getHome(name).getLocation().getDimensionType().toString());
                return;
            }

            Home home = homeHandler.getHome(name);

            try {
                homeHandler.teleportToHome(src, name);
            } catch (final UnsafeHomeException e) {
                if (e.getReason() == UserHomeHandler.Reason.MISSING_DIMENSION) {
                    src.sendError(e.getMessage());
                }
            }

            if (CommandUtils.areTheSame(src, user)) {
                src.sendLangMessage("command.home.teleport.self", home.getName());
            } else {
                src.sendLangMessage("command.home.teleport.other", home.getName(), user.getDisplayName());
            }
        });

        return IEssentialCommand.AWAIT;
    }

    private Component getTeleportConfirmationText(String homeName, String owner) {
        return new TextComponent("")
                .append(StringText.of("general.loc.unsafe.confirmation")
                        .withStyle(ChatFormatting.YELLOW))
                .append(new TextComponent(" [").withStyle(ChatFormatting.GRAY)
                        .append(new TextComponent("Click here to Confirm").withStyle(ChatFormatting.GREEN))
                        .append(new TextComponent("]").withStyle(ChatFormatting.GRAY))
                        .withStyle((style) -> {
                            return style.applyFormat(ChatFormatting.GRAY).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("Confirm").withStyle(ChatFormatting.YELLOW))).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/home -confirmed-" + homeName + " " + owner));
                        }));
    }

}
