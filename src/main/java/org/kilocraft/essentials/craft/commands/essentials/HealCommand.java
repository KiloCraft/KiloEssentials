package org.kilocraft.essentials.craft.commands.essentials;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.api.util.CommandSuggestions;
import org.kilocraft.essentials.craft.KiloCommands;
import org.kilocraft.essentials.craft.chat.KiloChat;

public class HealCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        KiloCommands.getCommandPermission("heal.self");
        KiloCommands.getCommandPermission("heal.other");
        LiteralArgumentBuilder<ServerCommandSource> heal = CommandManager.literal("heal");
        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> target = CommandManager.argument("target", EntityArgumentType.player())
                .suggests((context, builder) -> CommandSuggestions.allPlayers.getSuggestions(context, builder));

        heal.requires(s -> Thimble.hasPermissionChildOrOp(s, KiloCommands.getCommandPermission("heal.self"), 2));
        target.requires(s -> Thimble.hasPermissionChildOrOp(s, KiloCommands.getCommandPermission("heal.other"), 2));

        heal.executes(context -> execute(context.getSource(), context.getSource().getPlayer()));
        target.executes(context -> execute(context.getSource(), EntityArgumentType.getPlayer(context, "target")));

        heal.then(target);
        dispatcher.register(heal);
    }

    private static int execute(ServerCommandSource source, ServerPlayerEntity player) {

        if (source.getName().equals(player.getName().asString())){
            if (player.getHealth() == player.getMaximumHealth())
                KiloChat.sendMessageTo(player, LangText.get(true, "command.heal.exception.self"));
            else {
                KiloChat.sendMessageTo(player, LangText.get(true, "command.heal.self"));
            }
        } else {
            if (player.getHealth() == player.getMaximumHealth()) {
                KiloChat.sendMessageTo(source, LangText.getFormatter(true, "command.heal.exception.others", player.getName().asString()));
            } else {
                KiloChat.sendMessageTo(player, LangText.getFormatter(true, "command.heal.announce", source.getName()));
                TextFormat.sendToUniversalSource(source, LangText.getFormatter(true, "command.heal.other", player.getName().toString()), false);
            }
        }

        player.setHealth(player.getMaximumHealth());

        return 1;
    }
}