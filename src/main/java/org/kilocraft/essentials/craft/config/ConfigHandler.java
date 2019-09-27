package org.kilocraft.essentials.craft.config;

import org.kilocraft.essentials.api.Mod;
import org.kilocraft.essentials.api.config.ConfigFile;
import org.kilocraft.essentials.craft.KiloEssentials;

import java.io.File;
import java.util.ArrayList;

public class ConfigHandler {
    public static ArrayList<String> configFiles = new ArrayList<String>(){{
        add("general.yml");
        add("messages.yml");
        add("ranks.yml");
    }};

    public static void handle() {
        Mod.getLogger().info("Config Directory set to: " + Configs.getConfigPath());
        configFiles.forEach((config) -> {
            new ConfigFile(
                    config,
                    "^KiloEssentials^config^".replace("^", File.separator),
                    "ConfigFiles",
                    false,
                    false
            );

            ConfigProvider.provide(
                    Configs.valueOf(config.toUpperCase().replace(".YML", "")).getFile()
            );
        });

        KiloEssentials.getInstance().getLogger().info("Successfully loaded the configuration files!");
        KiloEssentials.getInstance().getLogger().info(configFiles.toString());
    }
}
