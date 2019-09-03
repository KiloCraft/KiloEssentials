package org.kilocraft.essentials.craft.config;

import org.kilocraft.essentials.api.Config.ConfigFile;

import java.io.File;
import java.util.ArrayList;

public class ConfigHandler {
    public static ArrayList<String> configFiles = new ArrayList<String>(){{
        add("General.yml");
        add("Messages.yml");
        add("Ranks.yml");
    }};

    private static String configPath = "^KiloEssentials^Config^".replace("^", File.separator);
    public static void handle() {

        configFiles.forEach((config) -> new ConfigFile(
                config,
                "^KiloEssentials^Config^".replace("^", File.separator),
                "ConfigFiles",
                false,
                true
        ));

    }
}
