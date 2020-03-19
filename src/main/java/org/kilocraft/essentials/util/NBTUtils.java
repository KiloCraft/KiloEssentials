package org.kilocraft.essentials.util;

import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.nbt.*;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.KiloEssentials;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class NBTUtils {
    public static void putUUID(CompoundTag tag, String key, UUID uuid) {
        tag.putUuidNew(key, uuid);
    }

    public static UUID getUUID(CompoundTag tag, String key) {
        UUID uuid;
        try {
            uuid = NbtHelper.toUuidNew(tag.getCompound(key));
        } catch (Exception ignored) {
            uuid = tag.getUuidNew(key);
        }
        return uuid;
    }

    @Nullable
    public static CompoundTag getPlayerTag(UUID uuid) {
        File file = new File(KiloEssentials.getWorkingDirectory() + "world/playerdata/" + uuid.toString() + ".dat");

        if (!file.exists())
            return null;

        CompoundTag compoundTag = null;

        try {
            compoundTag = NbtIo.readCompressed(new FileInputStream(file));
        } catch (IOException ignored) { }

        return compoundTag;
    }

    public static boolean savePlayerFromTag(UUID uuid, CompoundTag tag) {
        File file = new File(KiloEssentials.getWorkingDirectory() + "world/playerdata/" + uuid.toString() + ".dat");
        if (!file.exists())
            return false;

        try {
            NbtIo.writeCompressed(tag, new FileOutputStream(file));
        } catch (IOException e) {
            return false;
        }

        return false;
    }

    @Nullable
    public static EnderChestInventory tagToEnderchest(CompoundTag tag) {
        if (!tag.contains("EnderItems"))
            return null;

        EnderChestInventory inv = new EnderChestInventory();
        inv.readTags(tag.getList("EnderItems", 10));
        return inv;
    }

    public static void savePlayerEnderchest(UUID uuid, EnderChestInventory inv) {
        CompoundTag tag = getPlayerTag(uuid);
        if (tag == null)
            return;

        tag.put("EnderItems", inv.getTags());
        savePlayerFromTag(uuid, tag);
    }

    public static void setPlayerCustomName(UUID uuid, Text text) {
        CompoundTag tag = getPlayerTag(uuid);
        if (tag == null)
            return;

        tag.putString("CustomName", Text.Serializer.toJson(text));
        savePlayerFromTag(uuid, tag);
    }

}
