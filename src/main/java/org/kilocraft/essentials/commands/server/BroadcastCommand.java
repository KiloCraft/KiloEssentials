package org.kilocraft.essentials.commands.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.chat.ChatMessage;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.config.KiloConfig;

public class BroadcastCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> argumentBuilder = CommandManager.literal("broadcast")
                .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("broadcast"), 3))
                .then(
                        CommandManager.argument("message", StringArgumentType.greedyString())
                                .executes(c -> execute(StringArgumentType.getString(c, "message")))
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