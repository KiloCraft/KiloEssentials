package org.kilocraft.essentials.craft.commands.essentials;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.kilocraft.essentials.api.util.CommandHelper;
import org.kilocraft.essentials.api.util.CommandSuggestions;
import org.kilocraft.essentials.craft.chat.KiloChat;
import org.kilocraft.essentials.craft.provider.SimpleStringSaverProvider;

public class MessageCommand {
    private static final SimpleCommandExceptionType NO_MESSAGES_EXCEPTION = new SimpleCommandExceptionType(new LiteralText("You don't have any messages to reply to!"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> node = dispatcher.register(
                CommandManager.literal("ke_msg")
                        .then(
                                CommandManager.argument("player", EntityArgumentType.player())
                                        .suggests((context, builder) -> CommandSuggestions.allPlayers.getSuggestions(context, builder))
                                        .then(
                                                CommandManager.argument("message", StringArgumentType.greedyString())
                                                        .executes(c ->
                                                                executeSend(c.getSource(), EntityArgumentType.getPlayer(c, "player"), StringArgumentType.getString(c, "message"))
                                                        )
                                        )
                        )
        );

        LiteralCommandNode<ServerCommandSource> replyNode = dispatcher.register(
                CommandManager.literal("r")
                    .then(
                            CommandManager.argument("message", StringArgumentType.greedyString())
                                .executes(MessageCommand::executeReply)
                    )
        );

        dispatcher.register(CommandManager.literal("ke_tell").redirect(node));
        dispatcher.register(CommandManager.literal("ke_whisper").redirect(node));
        dispatcher.register(CommandManager.literal("reply").redirect(replyNode));

    }

    public static SimpleStringSaverProvider playerProvider = new SimpleStringSaverProvider();

    private static int executeReply(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        System.out.println(playerProvider.getValue(context.getSource().getName()));
        ServerPlayerEntity target = context.getSource().getMinecraftServer().getPlayerManager().getPlayer(playerProvider.getValue(context.getSource().getName()));
        String message = StringArgumentType.getString(context, "message");

        if (target == null)
            throw NO_MESSAGES_EXCEPTION.create();
        else
            executeSend(context.getSource(), target, message);

        return 1;
    }

    private static int executeSend(ServerCommandSource source, ServerPlayerEntity target, String message) throws CommandSyntaxException {
        if (!CommandHelper.areTheSame(source, target)) {
            playerProvider.save(source.getName(), target.getName().asString());
            KiloChat.sendPrivateMessageTo(source, target, message);
        } else
            source.sendError(new LiteralText("You can't send a message to your self!"));

        return 1;
    }

}
