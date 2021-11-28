package org.kilocraft.essentials.extensions.homes.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.api.world.location.Vec3dLocation;
import org.kilocraft.essentials.chat.StringText;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.extensions.homes.api.Home;
import org.kilocraft.essentials.user.UserHomeHandler;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.commands.CommandUtils;
import org.kilocraft.essentials.util.commands.KiloCommands;

import java.io.IOException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;

public class SethomeCommand extends EssentialCommand {
    public SethomeCommand() {
        super("sethome", CommandPermission.HOME_SELF_SET);
        this.withUsage("command.sethome.usage", "name");
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        RequiredArgumentBuilder<CommandSourceStack, String> homeArgument = this.argument("name", word())
                .executes(this::executeSelf);

        RequiredArgumentBuilder<CommandSourceStack, String> targetArgument = this.getUserArgument("user")
                .requires(src -> this.hasPermission(src, CommandPermission.HOME_OTHERS_SET))
                .executes(this::executeOthers);

        homeArgument.then(targetArgument);
        this.commandNode.addChild(homeArgument.build());
    }

    private int executeSelf(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        OnlineUser user = this.getOnlineUser(player);
        UserHomeHandler homeHandler = user.getHomesHandler();
        String input = getString(ctx, "name");
        String name = input.replaceFirst("-confirmed-", "");

        if (!canSet(user) && !homeHandler.hasHome(name)) {
            user.sendLangMessage("command.sethome.limit");
            return FAILED;
        }

        if (!Level.isInSpawnableBounds(player.blockPosition())) {
            user.sendLangError("general.position_out_of_world");
            return FAILED;
        }

        if (homeHandler.hasHome(name) && !input.startsWith("-confirmed-")) {
            user.sendMessage(this.getConfirmationText(name, ""));
            return AWAIT;
        } else {
            homeHandler.removeHome(name);
        }

        homeHandler.addHome(new Home(player.getUUID(), name, Vec3dLocation.of(player).shortDecimals()));
        user.sendLangMessage("command.sethome.self", name);

        return SUCCESS;
    }

    private int executeOthers(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        OnlineUser source = this.getOnlineUser(player);
        String inputName = getString(ctx, "user");
        String input = getString(ctx, "name");
        String name = input.replaceFirst("-confirmed-", "");

        if (!Level.isInSpawnableBounds(player.blockPosition())) {
            source.sendLangError("general.position_out_of_world");
            return FAILED;
        }

        this.getUserManager().getUserThenAcceptAsync(player, inputName, (user) -> {
            UserHomeHandler homeHandler = user.getHomesHandler();

            if (CommandUtils.areTheSame(source, user) && canSet(user) && !homeHandler.hasHome(name)) {
                source.sendLangMessage("command.sethome.limit");
                return;
            }

            if (homeHandler.hasHome(name) && !input.startsWith("-confirmed-")) {
                source.sendMessage(this.getConfirmationText(name, user.getUsername()));
                return;
            } else {
                homeHandler.removeHome(name);
            }

            homeHandler.addHome(new Home(user.getUuid(), name, Vec3dLocation.of(player).shortDecimals()));

            try {
                user.saveData();
            } catch (IOException e) {
                source.sendLangError("exception.user_cant_save", user.getNameTag(), e.getMessage());
            }

            if (CommandUtils.areTheSame(source, user))
                source.sendLangMessage("command.sethome.self", name);
            else
                source.sendLangMessage("command.sethome.other", name, user.getDisplayName());
        });

        return AWAIT;
    }

    private static boolean canSet(User user) {
        if (KiloCommands.hasPermission(((OnlineUser) user).getCommandSource(), CommandPermission.HOME_SET_LIMIT_BYPASS))
            return true;

        return user.getHomesHandler().homes() < getHomeLimit(user);
    }

    /**
     * TODO: If this feature is more needed, a different solution to this would be a "path" system
     * kiloessentials.command.home.limit.<path>.x
     * Example:
     * Playtime earned homes = kiloessentials.command.home.limit.playtime.x
     * Donation earned homes = kiloessentials.command.home.limit.donate.x
    * */
    private static int getHomeLimit(User user) {
        return getHomePermissionLimit(user, "kiloessentials.command.home.limit.") +
                getHomePermissionLimit(user, "kiloessentials.command.home.limit.add.");
    }

    private static int getHomePermissionLimit(User user, String permission) {
        int homeLimit = 0;
        for (int i = 1; i <= KiloConfig.main().homesLimit; i++) {
            if (KiloEssentials.hasPermissionNode(((OnlineUser) user).getCommandSource(), permission + i)) {
                homeLimit = i;
            }
        }
        return homeLimit;
    }

    private Component getConfirmationText(String homeName, String user) {
        return new TextComponent("")
                .append(StringText.of("command.sethome.confirmation_message")
                        .withStyle(ChatFormatting.YELLOW))
                .append(new TextComponent(" [").withStyle(ChatFormatting.GRAY)
                        .append(new TextComponent("Click here to Confirm").withStyle(ChatFormatting.GREEN))
                        .append(new TextComponent("]").withStyle(ChatFormatting.GRAY))
                        .withStyle((style) -> {
                            return style.applyFormat(ChatFormatting.GRAY).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("Confirm").withStyle(ChatFormatting.YELLOW))).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sethome -confirmed-" + homeName + " " + user));
                        }));
    }

}
