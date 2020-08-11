package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Wearable;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.commands.CommandUtils;

import static net.minecraft.command.argument.EntityArgumentType.player;

public class HatCommand extends EssentialCommand {
    public HatCommand() {
        super("hat", CommandPermission.HAT_SELF);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> targetArgument = argument("target", player())
                .requires(src -> KiloCommands.hasPermission(src, CommandPermission.HAT_OTHERS))
                .suggests(ArgumentSuggestions::allPlayers)
                .executes(this::executeOthers);

        commandNode.addChild(targetArgument.build());
        argumentBuilder.executes(this::execute);
    }

    private int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        return hat(ctx, ctx.getSource().getPlayer());
    }

    private int executeOthers(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        return hat(ctx, EntityArgumentType.getPlayer(ctx, "target"));
    }

    private int hat(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity target) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        PlayerInventory inventory = target.inventory;
        ItemStack handStack = inventory.getMainHandStack();

        if (handStack.getItem() instanceof Wearable) {
            KiloChat.sendLangMessageTo(player, "command.hat.invalid_item");
            return FAILED;
        }

        ItemStack head = inventory.armor.get(EquipmentSlot.HEAD.getEntitySlotId());

        target.setStackInHand(Hand.MAIN_HAND, head);
        inventory.armor.set(EquipmentSlot.HEAD.getEntitySlotId(), handStack);

        if (CommandUtils.areTheSame(player, target))
            KiloChat.sendLangMessageTo(player, "command.hat");
        else {
            KiloChat.sendLangMessageTo(player, "command.hat.others", target.getEntityName());
            KiloChat.sendLangMessageTo(target, "command.hat.announce", player.getEntityName());
        }


        return SUCCESS;
    }
}
