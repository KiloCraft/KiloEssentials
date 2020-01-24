package org.kilocraft.essentials.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Hand;
import org.kilocraft.essentials.api.KiloEssentials;

public class NbtCommands {
    public static boolean fromRightClick(PlayerEntity player, Hand hand) {
        return trigger(player, hand, !player.isHandSwinging);
    }

    private static boolean trigger(PlayerEntity player, Hand hand, boolean swingHand) {
        if (hand == Hand.OFF_HAND)
            return false;

        ItemStack stack = player.getMainHandStack();
        CompoundTag tag = stack.getTag();

        if (tag == null || tag.isEmpty() || !tag.contains("NBTCommands") && !player.isSneaking())
            return false;

        if (swingHand && !player.isHandSwinging)
            player.swingHand(Hand.MAIN_HAND, true);

        ListTag listTag = tag.getList("NBTCommands", 8);

        for (int i = 0; i < listTag.size(); i++) {
            KiloEssentials.getServer().execute(player.getCommandSource(), listTag.getString(i));
        }

        return true;
    }

}
