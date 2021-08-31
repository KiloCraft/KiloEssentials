package org.kilocraft.essentials.util.commands.play;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.EntitySelector;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.commands.CommandUtils;
import org.kilocraft.essentials.util.commands.KiloCommands;

import static net.minecraft.command.argument.EntityArgumentType.getPlayer;
import static net.minecraft.command.argument.EntityArgumentType.player;

public class FeedCommand extends EssentialCommand {
    public FeedCommand() {
        super("feed", CommandPermission.FEED_SELF);
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> target = argument("target", player())
                .requires(s -> KiloCommands.hasPermission(s, CommandPermission.FEED_OTHERS))
                .suggests(ArgumentSuggestions::allPlayers)
                .executes(context -> execute(context, getPlayer(context, "target")));

        argumentBuilder.executes(context -> execute(context, context.getSource().getPlayer()));
        commandNode.addChild(target.build());
    }

    private int execute(CommandContext<ServerCommandSource> context, ServerPlayerEntity player) {
        OnlineUser self = getCommandSource(context);
        OnlineUser target = getOnlineUser(player);
        if (CommandUtils.areTheSame(self, target)) {
            if (player.getHungerManager().getFoodLevel() == 20)
                target.sendLangMessage("command.feed.exception.self");
            else {
                target.sendLangMessage("command.feed.self");
            }
        } else {
            if (player.getHungerManager().getFoodLevel() == 20) {
                self.sendLangMessage("command.feed.exception.others", target.getFormattedDisplayName());
            } else {
                target.sendLangMessage("command.feed.announce", self.getDisplayName());
                self.sendLangMessage("command.feed.other", target.getDisplayName());
            }
        }

        player.getHungerManager().setFoodLevel(20);

        return 1;
    }
}
