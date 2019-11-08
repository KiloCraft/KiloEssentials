package org.kilocraft.essentials.commands.essentials;

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
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.util.CommandHelper;
import org.kilocraft.essentials.api.util.CommandSuggestions;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.provider.SimpleStringSaverProvider;

import java.util.concurrent.atomic.AtomicReference;

public class MessageCommand {
    private static final SimpleCommandExceptionType NO_MESSAGES_EXCEPTION = new SimpleCommandExceptionType(new LiteralText("You don't have any messages to reply to!"));
    private static final SimpleCommandExceptionType DERP_EXCEPTION = new SimpleCommandExceptionType(new LiteralText("You can't message your self you Derp!"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> node = dispatcher.register(
                CommandManager.literal("ke_msg")
                        .executes(context -> KiloCommands.executeUsageFor("command.message.usage", context.getSource()))
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
                        .executes(context -> KiloCommands.executeUsageFor("command.message.reply.usage", context.getSource()))
                        .then(
                            CommandManager.argument("message", StringArgumentType.greedyString())
                                .executes(MessageCommand::executeReply)
                    )
        );

        dispatcher.register(CommandManager.literal("ke_tell").redirect(node));
        dispatcher.register(CommandManager.literal("ke_whisper").redirect(node));
        dispatcher.register(CommandManager.literal("reply").redirect(replyNode));

    }

    public static SimpleStringSaverProvider stringSaverProvider = new SimpleStringSaverProvider();

    private static int executeReply(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        AtomicReference<ServerPlayerEntity> target = new AtomicReference<>();
        stringSaverProvider.getMap().forEach((key, value) -> {
            if (value.equals(context.getSource().getName())) target.set(KiloServer.getServer().getPlayer(key));
        });

        String message = StringArgumentType.getString(context, "message");

        if (target.get() == null)
            throw NO_MESSAGES_EXCEPTION.create();
        else
            executeSend(context.getSource(), target.get(), message);

        return 1;
    }

    private static int executeSend(ServerCommandSource source, ServerPlayerEntity target, String message) throws CommandSyntaxException {
        if (!CommandHelper.areTheSame(source, target)) {
            stringSaverProvider.save(target.getName().asString(), source.getName());
            KiloChat.sendPrivateMessageTo(source, target, message);
        } else
            throw DERP_EXCEPTION.create();

        return 1;
    }

}
