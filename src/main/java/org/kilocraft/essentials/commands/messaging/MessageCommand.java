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
import org.kilocraft.essentials.commands.CommandHelper;
import org.kilocraft.essentials.commands.CommandSuggestions;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.provided.SimpleStringSaverProvided;

import java.util.concurrent.atomic.AtomicReference;

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
        LiteralCommandNode<ServerCommandSource> node = dispatcher.register(
                literal("ke_msg")
                        .executes(context -> KiloCommands.executeUsageFor("command.message.usage", context.getSource()))
                        .then(
                                argument("player", player())
                                        .suggests(CommandSuggestions::allPlayers)
                                        .then(
                                                argument("message", greedyString())
                                                        .executes(c ->
                                                                executeSend(c.getSource(), getPlayer(c, "player"), getString(c, "message"))
                                                        )
                                        )
                        )
        );

        LiteralCommandNode<ServerCommandSource> replyNode = dispatcher.register(
                literal("r")
                        .executes(context -> KiloCommands.executeUsageFor("command.message.reply.usage", context.getSource()))
                        .then(
                            argument("message", greedyString())
                                .executes(MessageCommand::executeReply)
                    )
        );

        dispatcher.register(literal("ke_tell").redirect(node));
        dispatcher.register(literal("ke_whisper").redirect(node));
        dispatcher.register(literal("reply").redirect(replyNode));

    }

    public static SimpleStringSaverProvided stringSaverProvider = new SimpleStringSaverProvided();

    private static int executeReply(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        AtomicReference<ServerPlayerEntity> target = new AtomicReference<>();
        stringSaverProvider.getMap().forEach((key, value) -> {
            if (value.equals(context.getSource().getName())) target.set(KiloServer.getServer().getPlayer(key));
        });

        String message = getString(context, "message");

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
