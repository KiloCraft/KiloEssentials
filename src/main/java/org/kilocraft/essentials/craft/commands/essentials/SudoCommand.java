package org.kilocraft.essentials.craft.commands.essentials;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.craft.KiloCommands;

public class SudoCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> argumentBuilder = CommandManager.literal("sudo")
                .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("sudo"), 2))
                .then(CommandManager.argument("player", EntityArgumentType.player()));

        argumentBuilder.redirect(dispatcher.getRoot());

        dispatcher.register(argumentBuilder);
    }
}
