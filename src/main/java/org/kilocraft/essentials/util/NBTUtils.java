package org.kilocraft.essentials.util;

import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.config.KiloConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

public class NBTUtils {
    @Nullable
    public static CompoundTag getPlayerTag(UUID uuid) {
        File file = new File(KiloConfig.getWorkingDirectory() + "world/playerdata/" + uuid.toString() + ".dat");
        CompoundTag compoundTag = null;

        try {
            compoundTag = NbtIo.readCompressed(new FileInputStream(file));
        } catch (IOException ignored) { }

        return compoundTag;
    }

    @Nullable
    public EnderChestInventory tagToEnderchest(CompoundTag tag) {
        if (!tag.contains("EnderItems"))
            return null;

        EnderChestInventory inv = new EnderChestInventory();
        inv.readTags(tag.getList("EnderItems", 10));
        return inv;
    }

}
