package org.kilocraft.essentials.util.commands.play;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.EntitySelector;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.commands.KiloCommands;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.util.commands.CommandUtils;

import static net.minecraft.command.argument.EntityArgumentType.getPlayer;
import static net.minecraft.command.argument.EntityArgumentType.player;

public class HealCommand extends EssentialCommand {
    public HealCommand() {
        super("heal", CommandPermission.HEAL_SELF);
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> target = argument("target", player())
                .requires(s -> KiloCommands.hasPermission(s, CommandPermission.HEAL_OTHERS))
                .suggests(ArgumentSuggestions::allPlayers)
                .executes(context -> execute(context, getPlayer(context, "target")));
        
        argumentBuilder.executes(context -> execute(context, context.getSource().getPlayer()));
        commandNode.addChild(target.build());
    }

    private int execute(CommandContext<ServerCommandSource> context, ServerPlayerEntity player) {
        OnlineUser self = getCommandSource(context);
        OnlineUser target = getOnlineUser(player);
        boolean shouldHeal = player.getHealth() == player.getMaxHealth() && player.getHungerManager().getFoodLevel() == 20;
        if (CommandUtils.areTheSame(self, target)) {
            if (shouldHeal) {
                target.sendLangMessage("command.heal.exception.self");
            } else {
                target.sendLangMessage("command.heal.self");
            }
        } else {
            if (shouldHeal) {
                self.sendLangMessage("command.heal.exception.others", target.getFormattedDisplayName());
            } else {
                target.sendLangMessage("command.heal.announce", self.getDisplayName());
                self.sendLangMessage("command.heal.other", target.getDisplayName());
            }
        }

        player.setHealth(player.getMaxHealth());
        player.getHungerManager().setFoodLevel(20);

        return SUCCESS;
    }
}
