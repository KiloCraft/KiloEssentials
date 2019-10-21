package org.kilocraft.essentials.craft.commands.essentials;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.craft.KiloCommands;
import org.kilocraft.essentials.craft.player.KiloPlayer;
import org.kilocraft.essentials.craft.player.KiloPlayerManager;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class HealCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        KiloCommands.getCommandPermission("heal.self");
        KiloCommands.getCommandPermission("heal.other");
        LiteralArgumentBuilder<ServerCommandSource> heal = CommandManager.literal("heal");
        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> target = CommandManager.argument("target",
                EntityArgumentType.player());

        heal.requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("heal.self"), 2));
        target.requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("heal.other"), 2));

        heal.executes(context -> {
            execute(context.getSource().getPlayer(), context.getSource().getPlayer());
            return 0;
        });

        target.executes(context -> {
            execute(context.getSource().getPlayer(), EntityArgumentType.getPlayer(context, "target"));
            return 0;
        });

        heal.then(target);
        dispatcher.register(heal);
    }

    private static void execute(ServerPlayerEntity source, ServerPlayerEntity player) {
        KiloPlayer kiloPlayer = KiloPlayerManager.getPlayerData(player.getUuid());

        player.setHealth(player.getMaximumHealth());
        if(player == source){
            player.sendMessage(LangText.get(true, "command.heal.self"));
        }else{
            player.sendMessage(LangText.getFormatter(true, "command.heal.announce", source.getName().toString()));
            source.sendMessage(LangText.getFormatter(true, "command.heal.other", player.getName().toString()));
        }
    }
}