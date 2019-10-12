package org.kilocraft.essentials.craft.commands.essentials;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;
import org.kilocraft.essentials.craft.worldwarps.Warp;
import org.kilocraft.essentials.craft.worldwarps.WarpManager;

public class WarpCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager.literal("warp")
                .requires(s -> Thimble.hasPermissionChildOrOp(s, "kiloessentials.command.warp", 2));

        RequiredArgumentBuilder<ServerCommandSource, String> warpArg = CommandManager.argument("warp", StringArgumentType.string());

        builder.then(warpArg);
        dispatcher.register(builder);
    }

    private int executeAdd(ServerCommandSource source, String name, boolean permission, BlockPos pos) {
        WarpManager.INSTANCE.addWarp(
                new Warp(
                        name,
                        pos,
                        permission
                )
        );
        return 1;
    }
}
