package org.kilocraft.essentials.user;

import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.user.UserPlayer;
import org.kilocraft.essentials.config_old.KiloConfigOLD;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

public class ServerUserPlayer implements UserPlayer {
    private CompoundTag compoundTag;

    public ServerUserPlayer of(UUID uuid) {
        return new ServerUserPlayer(uuid);
    }

    private ServerUserPlayer(UUID uuid) {
        File file = new File(KiloConfigOLD.getWorkingDirectory() + "world/playerdata/" + uuid.toString() + ".dat");

        try {
            this.compoundTag = NbtIo.readCompressed(new FileInputStream(file));
        } catch (IOException ignored) { }
    }

    @Override
    public @Nullable EnderChestInventory getEnderchestInventory() {
        EnderChestInventory inv = new EnderChestInventory();
        inv.readTags(this.compoundTag.getList("EnderItems", 10));
        return inv;
    }

}
