package org.kilocraft.essentials.extensions.homes.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.command.IEssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.chat.ChatMessage;
import org.kilocraft.essentials.commands.CmdUtils;
import org.kilocraft.essentials.config.ConfigObjectReplacerUtil;
import org.kilocraft.essentials.config.ConfigVariableFactory;
import org.kilocraft.essentials.extensions.homes.api.Home;
import org.kilocraft.essentials.extensions.homes.api.UnsafeHomeException;
import org.kilocraft.essentials.user.UserHomeHandler;

public class HomeCommand extends EssentialCommand {
    private static final SimpleCommandExceptionType MISSING_DIMENSION = new SimpleCommandExceptionType(new LiteralText("The Dimension this home exists in no longer exists"));

    public HomeCommand() {
        super("home", CommandPermission.HOME_SELF_TP);
    }

    @Override
    public final void register(final CommandDispatcher<ServerCommandSource> dispatcher) {
        final RequiredArgumentBuilder<ServerCommandSource, String> homeArgument = this.argument("name", StringArgumentType.word())
                .suggests(UserHomeHandler::suggestHomes)
                .executes(this::executeSelf);

        final RequiredArgumentBuilder<ServerCommandSource, String> targetArgument = this.getUserArgument("user")
                .requires(src -> this.hasPermission(src, CommandPermission.HOME_OTHERS_TP))
                .executes(this::executeOthers);

        homeArgument.then(targetArgument);
        this.commandNode.addChild(homeArgument.build());
        this.withUsage("command.home.usage", "name");
    }

    private int executeSelf(final CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        final ServerPlayerEntity player = ctx.getSource().getPlayer();
        final OnlineUser user = this.getOnlineUser(player);
        final UserHomeHandler homeHandler = user.getHomesHandler();
        final String name = StringArgumentType.getString(ctx, "name");

        if (!homeHandler.hasHome(name)) {
            user.sendMessage(this.messages.commands().playerHomes().invalidHome);
            return IEssentialCommand.SINGLE_FAILED;
        }

        if (homeHandler.getHome(name).shouldTeleport()) {
            user.sendLangMessage("command.home.invalid_dim", homeHandler.getHome(name).getLocation().getDimensionType().toString());
            return IEssentialCommand.SINGLE_FAILED;
        }

        try {
            homeHandler.teleportToHome(user, name);
        } catch (final UnsafeHomeException e) {
            if (e.getReason() == UserHomeHandler.Reason.MISSING_DIMENSION)
                throw HomeCommand.MISSING_DIMENSION.create();
        }

        user.sendMessage(new ChatMessage(HomeCommand.replaceVariables(
                this.messages.commands().playerHomes().teleporting, user, user, user.getHomesHandler().getHome(name)), user));
        return IEssentialCommand.SINGLE_SUCCESS;
    }

    private int executeOthers(final CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        final ServerPlayerEntity player = ctx.getSource().getPlayer();
        final String name = StringArgumentType.getString(ctx, "name");
        final OnlineUser source = this.getOnlineUser(player);
        final String inputName = StringArgumentType.getString(ctx, "user");

        this.essentials.getUserThenAcceptAsync(source, inputName, user -> {
            final UserHomeHandler homeHandler = user.getHomesHandler();
            if (!homeHandler.hasHome(name)) {
                source.sendConfigMessage("commands.playerHomes.invalid_home");
                return;
            }

            if (homeHandler.getHome(name).shouldTeleport()) {
                source.sendLangMessage("command.home.invalid_dim", homeHandler.getHome(name).getLocation().getDimensionType().toString());
                return;
            }

            try {
                homeHandler.teleportToHome(source, name);
            } catch (final UnsafeHomeException e) {
                source.sendError(e.getMessage());
                return;
            }

            final String message = CmdUtils.areTheSame(source, user) ? this.messages.commands().playerHomes().teleporting :
                    this.messages.commands().playerHomes().admin().teleporting;

            source.sendMessage(new ChatMessage(HomeCommand.replaceVariables(
                    message, source, user, user.getHomesHandler().getHome(name)), user));
        });

        return IEssentialCommand.AWAIT_RESPONSE;
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

}
