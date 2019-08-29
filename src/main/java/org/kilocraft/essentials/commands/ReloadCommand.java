package org.kilocraft.essentials.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class ReloadCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> literalCommandNode = dispatcher.register(CommandManager.literal("reload")
            .requires(source -> source.hasPermissionLevel(3))
            .executes(context -> {
                return 1;
            })
        );

        dispatcher.register(CommandManager.literal("rl").redirect(literalCommandNode));
    }
}
