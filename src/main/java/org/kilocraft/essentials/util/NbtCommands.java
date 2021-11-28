package org.kilocraft.essentials.util;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.kilocraft.essentials.util.commands.CommandUtils;

public class NbtCommands {

    public static void registerEvents() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> fromRightClick(player, hand) ? InteractionResult.SUCCESS : InteractionResult.PASS);
        UseItemCallback.EVENT.register((player, world, hand) -> fromRightClick(player, hand) ? InteractionResultHolder.success(ItemStack.EMPTY) : InteractionResultHolder.pass(ItemStack.EMPTY));
    }

    public static boolean fromRightClick(Player player, InteractionHand hand) {
        return trigger(player, hand, !player.swinging);
    }

    private static boolean trigger(Player player, InteractionHand hand, boolean swingHand) {
        if (hand == InteractionHand.OFF_HAND) {
            return false;
        }

        ItemStack stack = player.getMainHandItem();
        CompoundTag tag = stack.getTag();

        if (tag == null || tag.size() == 0 || !tag.contains("NBTCommands")) {
            return false;
        }

        ListTag nbtList = tag.getList("NBTCommands", 8);

        int succeededExecutions = 0;
        for (int i = 0; i < nbtList.size(); i++) {
            int value = CommandUtils.runCommandWithFormatting(player.createCommandSourceStack(), nbtList.getString(i));
            if (value >= 1) {
                succeededExecutions++;
            }
        }

        if (succeededExecutions >= 1 && swingHand) {
            player.swing(InteractionHand.MAIN_HAND, true);
        }

        return true;
    }

}
