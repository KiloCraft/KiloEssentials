package org.kilocraft.essentials.extensions.warps.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.extensions.warps.Warp;
import org.kilocraft.essentials.extensions.warps.WarpManager;

import java.util.List;

public class WarpCommands {
    private static List<Warp> warps = WarpManager.getWarps();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        warps.forEach((warp) -> {
            LiteralArgumentBuilder<ServerCommandSource> argumentBuilder = CommandManager.literal(warp.getName());
            if (warp.doesRequirePermission()) buildPermission(argumentBuilder, warp);

            argumentBuilder.executes(context -> {
                return WarpManager.teleport(context.getSource(), warp);
            });

            dispatcher.register(argumentBuilder);
        });
    }

    private static void buildPermission(LiteralArgumentBuilder<ServerCommandSource> argumentBuilder, Warp warp) {
        argumentBuilder.requires(s -> Thimble.hasPermissionOrOp(s, warp.getPermissionNode() + "." + warp.getName(), 2));
    }
}
