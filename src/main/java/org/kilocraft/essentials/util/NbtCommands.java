package org.kilocraft.essentials.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.kilocraft.essentials.api.KiloEssentials;

public class NbtCommands {

    public static ActionResult trigger(PlayerEntity player, World world, ItemStack itemStack, Hand hand) {
        ItemStack stack = player.getMainHandStack();
        CompoundTag tag = stack.getTag();

        if (tag == null || tag.isEmpty() || !tag.contains("NBTCommands") && !player.isSneaking())
            return null;

        ListTag listTag = tag.getList("NBTCommands", 8);

        for (int i = 0; i < listTag.size(); i++) {
            String str = listTag.getString(i);
            KiloEssentials.getInstance().getCommandHandler().execute(player.getCommandSource(), str);
        }

        return ActionResult.SUCCESS;
    }

}
