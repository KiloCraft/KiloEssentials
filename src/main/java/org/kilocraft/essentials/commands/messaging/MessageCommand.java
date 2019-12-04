package org.kilocraft.essentials.commands.messaging;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.chat.ServerChat;
import org.kilocraft.essentials.commands.CommandHelper;

import java.util.UUID;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.command.arguments.EntityArgumentType.getPlayer;
import static net.minecraft.command.arguments.EntityArgumentType.player;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class MessageCommand {
    private static final SimpleCommandExceptionType NO_MESSAGES_EXCEPTION = new SimpleCommandExceptionType(new LiteralText("You don't have any messages to reply to!"));
    private static final SimpleCommandExceptionType DERP_EXCEPTION = new SimpleCommandExceptionType(new LiteralText("You can't message your self you Derp!"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> node = dispatcher.register(literal("ke_msg").executes(ctx ->
                KiloCommands.executeUsageFor("command.message.usage", ctx.getSource()))
                        .then(argument("player", player())
                                .suggests(ArgumentSuggestions::allPlayers)
                                .then(argument("message", greedyString()).executes(ctx ->
                                                executeSend(ctx.getSource(), getPlayer(ctx, "player"), getString(ctx, "message"))
                                )))
        );

        LiteralCommandNode<ServerCommandSource> replyNode = dispatcher.register(literal("r")
                        .executes(context -> KiloCommands.executeUsageFor("command.message.reply.usage", context.getSource()))
                        .then(
                            argument("message", greedyString())
                                .executes(MessageCommand::executeReply))
        );

        dispatcher.register(literal("ke_tell").redirect(node));
        dispatcher.register(literal("ke_whisper").redirect(node));
        dispatcher.register(literal("reply").redirect(replyNode));
    }


    private static int executeReply(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        OnlineUser user = KiloServer.getServer().getUserManager().getOnline(context.getSource());
        String message = getString(context, "message");
        UUID lastPMGetter = user.getLastPrivateMessageSender();

        if (lastPMGetter != null) {
            executeSend(context.getSource(), KiloServer.getServer().getPlayer(lastPMGetter), message);
        } else
            throw NO_MESSAGES_EXCEPTION.create();

        System.out.println(user.getLastPrivateMessageSender().toString());
        return 1;
    }

    private static int executeSend(ServerCommandSource source, ServerPlayerEntity target, String message) throws CommandSyntaxException {
        if  (!CommandHelper.isConsole(source)) {
            OnlineUser user = KiloServer.getServer().getUserManager().getOnline(source);
            user.setLastMessageSender(target.getUuid());
            user.setLastPrivateMessage(message);
        }

        if (!CommandHelper.areTheSame(source, target)) {
            ServerChat.sendPrivateMessage(source, KiloServer.getServer().getOnlineUser(target), message);
        }
        else
            throw DERP_EXCEPTION.create();

        return 1;
    }

}
