package org.kilocraft.essentials.util.nbt;

import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.KiloEssentials;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class NBTUtils {
    public static void putUUID(NbtCompound tag, String key, UUID uuid) {
        tag.putUuid(key, uuid);
    }

    public static UUID getUUID(NbtCompound tag, String key) {
        UUID uuid;
        //Get the UUID even if its the old format
        if (tag.contains(key + "Most") && tag.contains(key + "Least")) {
            uuid = new UUID(tag.getLong(key + "Most"), tag.getLong(key + "Least"));
        } else {
            uuid = tag.getUuid(key);
        }

        return uuid;
    }

    @Nullable
    public static NbtCompound getPlayerTag(UUID uuid) {
        File file = new File(KiloEssentials.getWorkingDirectory() + "world/playerdata/" + uuid.toString() + ".dat");

        if (!file.exists())
            return null;

        NbtCompound NbtCompound = null;

        try {
            NbtCompound = NbtIo.readCompressed(new FileInputStream(file));
        } catch (IOException ignored) { }

        return NbtCompound;
    }

    public static boolean savePlayerFromTag(UUID uuid, NbtCompound tag) {
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
    public static EnderChestInventory tagToEnderchest(NbtCompound tag) {
        if (!tag.contains("EnderItems"))
            return null;

        EnderChestInventory inv = new EnderChestInventory();
        inv.readTags(tag.getList("EnderItems", 10));
        return inv;
    }

    public static void savePlayerEnderchest(UUID uuid, EnderChestInventory inv) {
        NbtCompound tag = getPlayerTag(uuid);
        if (tag == null)
            return;

        tag.put("EnderItems", inv.getTags());
        savePlayerFromTag(uuid, tag);
    }

    public static void setPlayerCustomName(UUID uuid, Text text) {
        NbtCompound tag = getPlayerTag(uuid);
        if (tag == null)
            return;

        tag.putString("CustomName", Text.Serializer.toJson(text));
        savePlayerFromTag(uuid, tag);
    }

}
