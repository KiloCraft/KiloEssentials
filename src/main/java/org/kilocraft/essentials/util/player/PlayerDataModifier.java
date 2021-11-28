package org.kilocraft.essentials.util.player;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import org.kilocraft.essentials.api.KiloEssentials;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class PlayerDataModifier {
    private final UUID uuid;
    private CompoundTag NbtCompound;

    public PlayerDataModifier(UUID uuid) {
        this.uuid = uuid;
    }

    public boolean load() {
        File file = new File(KiloEssentials.getWorkingDirectory() + "world/playerdata/" + this.uuid.toString() + ".dat");
        if (!file.exists())
            return false;

        try {
            this.NbtCompound = NbtIo.readCompressed(new FileInputStream(file));
        } catch (IOException ignored) {
        }

        return true;
    }

    public boolean save() {
        File file = new File(KiloEssentials.getWorkingDirectory() + "world/playerdata/" + this.uuid.toString() + ".dat");
        if (!file.exists())
            return false;

        try {
            NbtIo.writeCompressed(this.NbtCompound, new FileOutputStream(file));
        } catch (IOException e) {
            return false;
        }

        return false;
    }

    public CompoundTag getNbt() {
        return this.NbtCompound;
    }

    public void setCustomName(Component text) {
        if (text != null)
            this.NbtCompound.putString("CustomName", Component.Serializer.toJson(text));
        else
            this.NbtCompound.remove("CustomName");
    }

    public PlayerEnderChestContainer getEnderChest() {
        PlayerEnderChestContainer inv = new PlayerEnderChestContainer();
        inv.fromTag(this.NbtCompound.getList("EnderItems", 10));
        return inv;
    }

}
