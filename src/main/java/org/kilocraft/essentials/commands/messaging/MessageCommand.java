package org.kilocraft.essentials.commands.messaging;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.command.TabCompletions;
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
import static org.kilocraft.essentials.KiloCommands.SUCCESS;
import static org.kilocraft.essentials.KiloCommands.executeUsageFor;

public class MessageCommand {
    private static final SimpleCommandExceptionType NO_MESSAGES_EXCEPTION = new SimpleCommandExceptionType(new LiteralText("You don't have any messages to reply to!"));
    private static final SimpleCommandExceptionType SAME_TARGETS_EXCEPTION = new SimpleCommandExceptionType(new LiteralText("You can't message your self!"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> node = dispatcher.register(literal("ke_msg").executes(ctx ->
                executeUsageFor("command.message.usage", ctx.getSource()))
                        .then(argument("player", player()).suggests(TabCompletions::allPlayers)
                                .then(argument("message", greedyString())
                                        .suggests(TabCompletions::noSuggestions)
                                        .executes(ctx ->
                                                executeSend(ctx.getSource(), getPlayer(ctx, "player"), getString(ctx, "message"))))));

        LiteralCommandNode<ServerCommandSource> replyNode = dispatcher.register(literal("r")
                        .executes(context -> executeUsageFor("command.message.reply.usage", context.getSource()))
                        .then(argument("message", greedyString())
                                .suggests(TabCompletions::noSuggestions)
                                .executes(MessageCommand::executeReply)));

        dispatcher.register(literal("ke_tell").redirect(node));
        dispatcher.register(literal("ke_whisper").redirect(node));
        dispatcher.register(literal("reply").redirect(replyNode));
    }


    private static int executeReply(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        OnlineUser user = KiloServer.getServer().getUserManager().getOnline(context.getSource());
        String message = getString(context, "message");
        UUID lastPMGetter = user.getLastPrivateMessageSender();

        if (lastPMGetter == null)
            throw NO_MESSAGES_EXCEPTION.create();

        return executeSend(context.getSource(), KiloServer.getServer().getPlayer(lastPMGetter), message);
    }

    private static int executeSend(ServerCommandSource source, ServerPlayerEntity target, String message) throws CommandSyntaxException {
        if  (!CommandHelper.isConsole(source)) {
            OnlineUser user = KiloServer.getServer().getOnlineUser(target);
            user.setLastMessageSender(source.getPlayer().getUuid());
            user.setLastPrivateMessage(message);
        }

        if (CommandHelper.areTheSame(source, target))
            throw SAME_TARGETS_EXCEPTION.create();

        ServerChat.sendPrivateMessage(source, KiloServer.getServer().getOnlineUser(target), message);

        return SUCCESS();
    }

}
