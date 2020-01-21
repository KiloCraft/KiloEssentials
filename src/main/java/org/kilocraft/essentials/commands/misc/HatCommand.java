package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.EntitySelector;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.command.TabCompletions;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.commands.CommandHelper;

import static net.minecraft.command.arguments.EntityArgumentType.getPlayer;
import static net.minecraft.command.arguments.EntityArgumentType.player;

public class HatCommand extends EssentialCommand {
    public HatCommand() {
        super("hat", CommandPermission.HAT_SELF);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> targetArgument = argument("target", player())
                .requires(src -> KiloCommands.hasPermission(src, CommandPermission.HAT_OTHERS))
                .suggests(TabCompletions::allPlayers)
                .executes(this::executeOthers);

        commandNode.addChild(targetArgument.build());
        argumentBuilder.executes(this::execute);
    }

    private int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();

        if (player.getMainHandStack().isEmpty()) {
            KiloChat.sendLangMessageTo(player, "general.no_item");
            return SINGLE_FAILED;
        }

        PlayerInventory inventory = player.inventory;
        ItemStack hand = inventory.getMainHandStack();
        ItemStack head = inventory.armor.get(EquipmentSlot.HEAD.getEntitySlotId());

        player.setStackInHand(Hand.MAIN_HAND, head);
        inventory.armor.set(EquipmentSlot.HEAD.getEntitySlotId(), hand);

        KiloChat.sendLangMessageTo(player, "command.hat");
        return SINGLE_SUCCESS;
    }

    private int executeOthers(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        ServerPlayerEntity target = getPlayer(ctx, "target");
        PlayerInventory inventory = target.inventory;
        ItemStack hand = inventory.getMainHandStack();
        ItemStack head = inventory.armor.get(EquipmentSlot.HEAD.getEntitySlotId());

        target.setStackInHand(Hand.MAIN_HAND, head);
        inventory.armor.set(EquipmentSlot.HEAD.getEntitySlotId(), hand);

        if (CommandHelper.areTheSame(player, target))
            KiloChat.sendLangMessageTo(player, "command.hat");
        else {
            KiloChat.sendLangMessageTo(player, "command.hat.others", target.getEntityName());
            KiloChat.sendLangMessageTo(target, "command.hat.announce", player.getEntityName());
        }

        return SINGLE_SUCCESS;
    }
}
