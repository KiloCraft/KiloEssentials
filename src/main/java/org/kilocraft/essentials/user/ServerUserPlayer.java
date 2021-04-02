package org.kilocraft.essentials.user;

import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.user.UserPlayer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

public class ServerUserPlayer implements UserPlayer {
    private NbtCompound NbtCompound;

    public ServerUserPlayer of(UUID uuid) {
        return new ServerUserPlayer(uuid);
    }

    private ServerUserPlayer(UUID uuid) {
        File file = new File(KiloEssentials.getWorkingDirectory() + "world/playerdata/" + uuid.toString() + ".dat");

        try {
            this.NbtCompound = NbtIo.readCompressed(new FileInputStream(file));
        } catch (IOException ignored) { }
    }

    @Override
    public @Nullable EnderChestInventory getEnderchestInventory() {
        EnderChestInventory inv = new EnderChestInventory();
        inv.readTags(this.NbtCompound.getList("EnderItems", 10));
        return inv;
    }

}
