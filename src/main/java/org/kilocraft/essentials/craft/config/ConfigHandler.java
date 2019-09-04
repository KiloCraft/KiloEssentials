package org.kilocraft.essentials.craft.config;

import org.kilocraft.essentials.api.config.ConfigFile;

import java.io.File;
import java.util.ArrayList;

public class ConfigHandler {
    public static ArrayList<String> configFiles = new ArrayList<String>(){{
        add("General.yml");
        add("Messages.yml");
        add("Ranks.yml");
    }};

    private static String configPath = "^KiloEssentials^config^".replace("^", File.separator);
    public static void handle() {

        configFiles.forEach((config) -> new ConfigFile(
                config,
                "^KiloEssentials^config^".replace("^", File.separator),
                "ConfigFiles",
                false,
                true
        ));

    }
}
