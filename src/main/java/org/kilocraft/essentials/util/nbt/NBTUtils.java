package org.kilocraft.essentials.util.nbt;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.KiloEssentials;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class NBTUtils {
    public static void putUUID(CompoundTag tag, String key, UUID uuid) {
        tag.putUUID(key, uuid);
    }

    public static UUID getUUID(CompoundTag tag, String key) {
        UUID uuid;
        // Get the UUID even if its the old format
        if (tag.contains(key + "Most") && tag.contains(key + "Least")) {
            uuid = new UUID(tag.getLong(key + "Most"), tag.getLong(key + "Least"));
        } else {
            uuid = tag.getUUID(key);
        }

        return uuid;
    }

    @Nullable
    public static CompoundTag getPlayerTag(UUID uuid) {
        File file = new File(KiloEssentials.getWorkingDirectory() + "world/playerdata/" + uuid.toString() + ".dat");

        if (!file.exists())
            return null;

        CompoundTag NbtCompound = null;

        try {
            NbtCompound = NbtIo.readCompressed(new FileInputStream(file));
        } catch (IOException ignored) {
        }

        return NbtCompound;
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
    public static PlayerEnderChestContainer tagToEnderchest(CompoundTag tag) {
        if (!tag.contains("EnderItems"))
            return null;

        PlayerEnderChestContainer inv = new PlayerEnderChestContainer();
        inv.fromTag(tag.getList("EnderItems", 10));
        return inv;
    }

    public static void savePlayerEnderchest(UUID uuid, PlayerEnderChestContainer inv) {
        CompoundTag tag = getPlayerTag(uuid);
        if (tag == null)
            return;

        tag.put("EnderItems", inv.createTag());
        savePlayerFromTag(uuid, tag);
    }

    public static void setPlayerCustomName(UUID uuid, Component text) {
        CompoundTag tag = getPlayerTag(uuid);
        if (tag == null)
            return;

        tag.putString("CustomName", Component.Serializer.toJson(text));
        savePlayerFromTag(uuid, tag);
    }

}
