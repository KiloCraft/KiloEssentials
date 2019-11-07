package org.kilocraft.essentials.craft.worldwarps;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;

import static net.minecraft.server.command.CommandManager.literal;

public class WarpCommands { // TODO Again why is not in commands package
    private static List<Warp> warps = WarpManager.getWarps();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        warps.forEach((warp) -> {
            LiteralArgumentBuilder<ServerCommandSource> argumentBuilder = literal(warp.getName());
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
