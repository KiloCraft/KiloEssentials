package org.kilocraft.essentials.commands.Essentials;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class HomeCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> literalCommandNode = CommandManager.literal("home")
                .requires(source -> source.hasPermissionLevel(1)).executes(context -> {
                    return 1;
                }).build();



    }
}
