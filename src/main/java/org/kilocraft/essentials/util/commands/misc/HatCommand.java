package org.kilocraft.essentials.util.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Wearable;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.commands.CommandUtils;
import org.kilocraft.essentials.util.commands.KiloCommands;

import static net.minecraft.commands.arguments.EntityArgument.player;

public class HatCommand extends EssentialCommand {
    public HatCommand() {
        super("hat", CommandPermission.HAT_SELF);
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        RequiredArgumentBuilder<CommandSourceStack, EntitySelector> targetArgument = this.argument("target", player())
                .requires(src -> KiloCommands.hasPermission(src, CommandPermission.HAT_OTHERS))
                .suggests(ArgumentSuggestions::allPlayers)
                .executes(this::executeOthers);

        this.commandNode.addChild(targetArgument.build());
        this.argumentBuilder.executes(this::execute);
    }

    private int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return this.hat(ctx, ctx.getSource().getPlayerOrException());
    }

    private int executeOthers(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return this.hat(ctx, EntityArgument.getPlayer(ctx, "target"));
    }

    private int hat(CommandContext<CommandSourceStack> ctx, ServerPlayer target) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        Inventory inventory = target.getInventory();
        ItemStack handStack = inventory.getSelected();
        OnlineUser user = this.getOnlineUser(ctx);

        if (handStack.getItem() instanceof Wearable) {
            user.sendLangMessage("command.hat.invalid_item");
            return FAILED;
        }

        ItemStack head = inventory.armor.get(EquipmentSlot.HEAD.getIndex());

        target.setItemInHand(InteractionHand.MAIN_HAND, head);
        inventory.armor.set(EquipmentSlot.HEAD.getIndex(), handStack);

        if (CommandUtils.areTheSame(player, target))
            user.sendLangMessage("command.hat");
        else {
            user.sendLangMessage("command.hat.others", target.getScoreboardName());
            this.getOnlineUser(target).sendLangMessage("command.hat.announce", player.getScoreboardName());
        }


        return SUCCESS;
    }
}
