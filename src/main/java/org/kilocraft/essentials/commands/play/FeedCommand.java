package org.kilocraft.essentials.commands.play;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.command.EntitySelector;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.api.command.TabCompletions;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.commands.CommandHelper;

import static net.minecraft.command.arguments.EntityArgumentType.getPlayer;
import static net.minecraft.command.arguments.EntityArgumentType.player;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class FeedCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> feed = literal("feed")
                .requires(s -> KiloCommands.hasPermission(s, CommandPermission.FEED_SELF));
        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> target = argument("target", player())
                .requires(s -> KiloCommands.hasPermission(s, CommandPermission.FEED_OTHERS))
                .suggests(TabCompletions::allPlayers);

        feed.executes(context -> execute(context.getSource(), context.getSource().getPlayer()));
        target.executes(context -> execute(context.getSource(), getPlayer(context, "target")));

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
                KiloChat.sendMessageToSource(source, LangText.getFormatter(true, "command.feed.other", player.getName().asString()));
            }
        }

        player.getHungerManager().setFoodLevel(20);

        return 1;
    }
}
