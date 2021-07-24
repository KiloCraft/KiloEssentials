package org.kilocraft.essentials.extensions.homes.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.command.IEssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.api.util.ScheduledExecutionThread;
import org.kilocraft.essentials.chat.StringText;
import org.kilocraft.essentials.util.commands.CommandUtils;
import org.kilocraft.essentials.config.ConfigObjectReplacerUtil;
import org.kilocraft.essentials.config.ConfigVariableFactory;
import org.kilocraft.essentials.extensions.homes.api.Home;
import org.kilocraft.essentials.extensions.homes.api.UnsafeHomeException;
import org.kilocraft.essentials.user.UserHomeHandler;

public class HomeCommand extends EssentialCommand {
    private static final SimpleCommandExceptionType MISSING_DIMENSION = new SimpleCommandExceptionType(new LiteralText("The Dimension this home exists in no longer exists"));

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
    public final void register(final CommandDispatcher<ServerCommandSource> dispatcher) {
        final RequiredArgumentBuilder<ServerCommandSource, String> homeArgument = this.argument("name", StringArgumentType.word())
                .suggests(UserHomeHandler::suggestHomes)
                .executes(ctx -> executeSelf(ctx, true));

        final RequiredArgumentBuilder<ServerCommandSource, String> targetArgument = this.getUserArgument("user")
                .requires(src -> this.hasPermission(src, CommandPermission.HOME_OTHERS_TP))
                .executes(this::executeOthers);

        homeArgument.then(targetArgument);
        argumentBuilder.executes(ctx -> executeSelf(ctx, false));
        this.commandNode.addChild(homeArgument.build());
    }

    private int executeSelf(final CommandContext<ServerCommandSource> ctx, boolean hasInput) throws CommandSyntaxException {
        final ServerPlayerEntity player = ctx.getSource().getPlayer();
        final OnlineUser user = this.getOnlineUser(player);
        final UserHomeHandler homeHandler = user.getHomesHandler();
        final String input = hasInput ? StringArgumentType.getString(ctx, "name") : "home";
        final String name = input.replaceFirst("-confirmed-", "");

        if (!homeHandler.hasHome(name)) {
            user.sendLangMessage("command.home.invalid_home");
            return IEssentialCommand.FAILED;
        }

        if (homeHandler.getHome(name).shouldTeleport()) {
            user.sendLangMessage("command.home.invalid_dim", homeHandler.getHome(name).getLocation().getDimensionType().toString());
            return IEssentialCommand.FAILED;
        }

        ScheduledExecutionThread.teleport(user, null, () -> {
            try {
                if (user.isOnline()) {
                    homeHandler.teleportToHome(user, name);
                    user.sendLangMessage("command.home.teleport.self", name);
                }
            } catch (final UnsafeHomeException e) {
                if (e.getReason() == UserHomeHandler.Reason.MISSING_DIMENSION) {
                    user.sendError(e.getMessage());
                }
            }
        });

        return IEssentialCommand.SUCCESS;
    }

    private int executeOthers(final CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        final ServerPlayerEntity player = ctx.getSource().getPlayer();
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

//            try {
//                LocationUtil.validateIsSafe(home.getLocation());
//            } catch (InsecureDestinationException e) {
//                if (!input.startsWith("-confirmed-")) {
//                    source.sendMessage(getTeleportConfirmationText(name, user.getUsername()));
//                    return;
//                }
//            }

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

    private Text getTeleportConfirmationText(String homeName, String owner) {
        return new LiteralText("")
                .append(StringText.of(true, "general.loc.unsafe.confirmation")
                        .formatted(Formatting.YELLOW))
                .append(new LiteralText(" [").formatted(Formatting.GRAY)
                        .append(new LiteralText("Click here to Confirm").formatted(Formatting.GREEN))
                        .append(new LiteralText("]").formatted(Formatting.GRAY))
                        .styled((style) -> {
                            return style.withFormatting(Formatting.GRAY).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Confirm").formatted(Formatting.YELLOW))).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/home -confirmed-" + homeName + " " + owner));
                        }));
    }

}
