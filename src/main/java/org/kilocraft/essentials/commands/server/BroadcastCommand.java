package org.kilocraft.essentials.commands.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.chat.ChatMessage;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.config.KiloConfig;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.server.command.CommandManager.literal;

public class BroadcastCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> argumentBuilder = literal("broadcast")
                .requires(s -> KiloCommands.hasPermission(s, CommandPermission.BROADCAST))
                .then(
                        CommandManager.argument("message", greedyString())
                                .executes(c -> execute(getString(c, "message")))
                );

        dispatcher.register(argumentBuilder);
    }

    private static int execute(String message) {
        String format = KiloConfig.getProvider().getMessages().getValue("commands.broadcast_format");
        System.out.println(format);
        KiloChat.broadCast(
                new ChatMessage(
                        format.replace("%MESSAGE%", message),
                        true
                )
        );
        return 1;
    }
}