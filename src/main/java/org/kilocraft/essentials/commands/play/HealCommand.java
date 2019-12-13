package org.kilocraft.essentials.commands.play;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.command.EntitySelector;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.api.command.TabCompletions;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.commands.CommandHelper;

import static net.minecraft.command.arguments.EntityArgumentType.getPlayer;
import static net.minecraft.command.arguments.EntityArgumentType.player;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class HealCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> heal = literal("heal")
                .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("heal.self"), 2));
        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> target = argument("target", player())
                .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("heal.others"), 2))
                .suggests(TabCompletions::allPlayers);

        heal.requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("heal.self"), 2));
        target.requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("heal.other"), 2));

        heal.executes(context -> execute(context.getSource(), context.getSource().getPlayer()));
        target.executes(context -> execute(context.getSource(), getPlayer(context, "target")));

        heal.then(target);
        dispatcher.register(heal);
    }

    private static int execute(ServerCommandSource source, ServerPlayerEntity player) {

        if (CommandHelper.areTheSame(source, player)) {
            if (player.getHealth() == player.getMaximumHealth()) {
                KiloChat.sendMessageTo(player, LangText.get(true, "command.heal.exception.self"));
            } else {
                KiloChat.sendMessageTo(player, LangText.get(true, "command.heal.self"));
            }
        } else {
            if (player.getHealth() == player.getMaximumHealth()) {
                KiloChat.sendMessageTo(source, LangText.getFormatter(true, "command.heal.exception.others", player.getName().asString()));
            } else {
                KiloChat.sendMessageTo(player, LangText.getFormatter(true, "command.heal.announce", source.getName()));
                KiloChat.sendMessageToSource(source, LangText.getFormatter(true, "command.heal.other", player.getName().asString()));
            }
        }

        player.setHealth(player.getMaximumHealth());
        player.getHungerManager().setFoodLevel(20);

        return 1;
    }
}
