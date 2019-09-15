package org.kilocraft.essentials.craft.config;

import org.kilocraft.essentials.api.Mod;
import org.kilocraft.essentials.api.config.ConfigFile;

import java.io.File;
import java.util.ArrayList;

public class ConfigHandler {
    public static ArrayList<String> configFiles = new ArrayList<String>(){{
        add("General.yml");
        add("Messages.yml");
        add("Ranks.yml");
        add("Database.yml");
    }};

    public static void handle() {
        Mod.getLogger().info("Config Directory set to: " + Configs.getConfigPath());
        configFiles.forEach((config) -> {
            new ConfigFile(
                    config,
                    "^KiloEssentials^config^".replace("^", File.separator),
                    "ConfigFiles",
                    false,
                    true
            );

            ConfigProvider.provide(
                    Configs.valueOf(config.toUpperCase().replace(".YML", "")).getFile()
            );
        });
    }
}
