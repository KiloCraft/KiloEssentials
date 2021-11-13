package org.kilocraft.essentials.util;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import org.kilocraft.essentials.util.commands.CommandUtils;

public class NbtCommands {

    public static void registerEvents() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> fromRightClick(player, hand) ? ActionResult.SUCCESS : ActionResult.PASS);
        UseItemCallback.EVENT.register((player, world, hand) -> fromRightClick(player, hand) ? TypedActionResult.success(ItemStack.EMPTY) : TypedActionResult.pass(ItemStack.EMPTY));
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
            player.swingHand(Hand.MAIN_HAND, true);
        }

        return true;
    }

}
