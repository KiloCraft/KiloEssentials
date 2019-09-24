package org.kilocraft.essentials.craft.config;

import org.kilocraft.essentials.api.Mod;
import org.kilocraft.essentials.api.config.NbtFile;

import java.io.File;

public class DataHandler {
    private static final String dir = "^KiloEssentials^data^".replace("^", File.separator);
    private static NbtFile homesNbt = new NbtFile(dir, "homes");

    public DataHandler() {
        Mod.getLogger().info("Loading the data files...");
    }

    public static NbtFile getHomes() {
        return homesNbt;
    }
}
