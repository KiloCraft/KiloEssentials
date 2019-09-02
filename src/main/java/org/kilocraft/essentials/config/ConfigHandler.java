package org.kilocraft.essentials.config;

import java.io.File;
import java.util.ArrayList;

public class ConfigHandler {
    private static String configPath = "^KiloEssentials^Config^".replace("^", File.separator);
    public static void handle() {
        ArrayList<String> configFiles = new ArrayList<String>(){{
            add("General.yml");
            add("Messages.yml");
        }};


        configFiles.forEach((config) -> new ConfigFile(
                config,
                "^KiloEssentials^Config^".replace("^", File.separator),
                "ConfigFiles",
                false,
                true
        ));

    }
}
