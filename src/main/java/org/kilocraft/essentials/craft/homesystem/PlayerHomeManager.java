package org.kilocraft.essentials.craft.homesystem;

import org.kilocraft.essentials.api.config.NbtFile;

public class PlayerHomeManager {
    public static NbtFile nbtFile = new NbtFile("/KiloEssentials/data/", "homes");

    public PlayerHomeManager() {

        nbtFile.load();
        nbtFile.getCompoundTag().putBoolean("isWorking", true);
        nbtFile.save();


    }
}
