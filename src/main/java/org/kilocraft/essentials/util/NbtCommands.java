package org.kilocraft.essentials.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.kilocraft.essentials.util.commands.CommandUtils;
import org.kilocraft.essentials.events.PlayerEvents;
import org.kilocraft.essentials.util.player.UserUtils;

public class NbtCommands {

    public static void registerEvents() {
        PlayerEvents.INTERACT_BLOCK.register((player, world, stack, hand, hitResult) -> fromRightClick(player, hand) ? ActionResult.SUCCESS : ActionResult.PASS);
        PlayerEvents.INTERACT_ITEM.register((player, world, stack, hand) -> fromRightClick(player, hand) ? ActionResult.SUCCESS : ActionResult.PASS);
    }

    public static boolean fromRightClick(PlayerEntity player, Hand hand) {
        return trigger(player, hand, !player.handSwinging);
    }

    private static boolean trigger(PlayerEntity player, Hand hand, boolean swingHand) {
        if (hand == Hand.OFF_HAND) {
            return false;
        }

        ItemStack stack = player.getMainHandStack();
        NbtCompound tag = stack.getNbt();

        if (tag == null || tag.getSize() == 0 || !tag.contains("NBTCommands")) {
            return false;
        }

        NbtList nbtList = tag.getList("NBTCommands", 8);

        int succeededExecutions = 0;
        for (int i = 0; i < nbtList.size(); i++) {
            int value = CommandUtils.runCommandWithFormatting(player.getCommandSource(), nbtList.getString(i));
            if (value >= 1) {
                succeededExecutions++;
            }
        }

        if (succeededExecutions >= 1 && swingHand) {
            UserUtils.Animate.swingHand(player);
        }

        return true;
    }

}
