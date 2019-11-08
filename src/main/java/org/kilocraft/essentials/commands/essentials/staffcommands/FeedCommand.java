package org.kilocraft.essentials.commands.essentials.staffcommands;

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
import org.kilocraft.essentials.api.util.CommandHelper;
import org.kilocraft.essentials.api.util.CommandSuggestions;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.chat.KiloChat;

public class FeedCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> feed = CommandManager.literal("feed")
                .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("feed.self"), 2));
        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> target = CommandManager.argument("target", EntityArgumentType.player())
                .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("feed.others"), 2))
                .suggests((context, builder) -> CommandSuggestions.allPlayers.getSuggestions(context, builder));

        feed.requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("feed.self"), 2));
        target.requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("feed.other"), 2));

        feed.executes(context -> execute(context.getSource(), context.getSource().getPlayer()));
        target.executes(context -> execute(context.getSource(), EntityArgumentType.getPlayer(context, "target")));

        feed.then(target);
        dispatcher.register(feed);
    }

    private static int execute(ServerCommandSource source, ServerPlayerEntity player) {

        if (CommandHelper.areTheSame(source, player)){
            if (player.getHungerManager().getFoodLevel() == 20)
                KiloChat.sendMessageTo(player, LangText.get(true, "command.feed.exception.self"));
            else {
                KiloChat.sendMessageTo(player, LangText.get(true, "command.feed.self"));
            }
        } else {
            if (player.getHealth() == player.getMaximumHealth()) {
                KiloChat.sendMessageTo(source, LangText.getFormatter(true, "command.feed.exception.others", player.getName().asString()));
            } else {
                KiloChat.sendMessageTo(player, LangText.getFormatter(true, "command.feed.announce", source.getName()));
                TextFormat.sendToUniversalSource(source, LangText.getFormatter(true, "command.feed.other", player.getName().toString()), false);
            }
        }

        player.getHungerManager().setFoodLevel(20);

        return 1;
    }
}