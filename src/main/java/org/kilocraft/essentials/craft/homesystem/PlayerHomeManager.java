package org.kilocraft.essentials.craft.homesystem;

import net.minecraft.nbt.CompoundTag;
import org.kilocraft.essentials.api.config.NbtFile;
import org.kilocraft.essentials.craft.config.DataHandler;

public class PlayerHomeManager {
    private static NbtFile nbtFile = DataHandler.getHomes();
    private static CompoundTag compoundTag = nbtFile.getCompoundTag();
    public PlayerHomeManager() {
        nbtFile.load();

        compoundTag.putInt("Test", 2);

        nbtFile.save();

        System.out.println(compoundTag.getInt("Test"));
    }
}
