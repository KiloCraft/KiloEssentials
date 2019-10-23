package org.kilocraft.essentials.craft.commands.essentials;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.craft.KiloCommands;
import org.kilocraft.essentials.craft.chat.ChatMessage;
import org.kilocraft.essentials.craft.chat.KiloChat;
import org.kilocraft.essentials.craft.config.KiloConifg;

public class BroadcastCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> argumentBuilder = CommandManager.literal("broadcast")
                .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("broadcast"), 2))
                .then(
                        CommandManager.argument("message", StringArgumentType.greedyString())
                                .executes(c -> execute(c.getSource(), StringArgumentType.getString(c, "message")))
                );

        dispatcher.register(argumentBuilder);
    }

    private static int execute(ServerCommandSource source, String message) {
        String format = KiloConifg.getProvider().getMessages().get(true, "command.broadcast.format");
        KiloChat.broadCast(
                new ChatMessage(
                        format.replace("%MESSAGE%", message),
                        true
                )
        );
        return 1;
    }
}