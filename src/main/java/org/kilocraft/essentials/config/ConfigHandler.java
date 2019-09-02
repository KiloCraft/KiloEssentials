package org.kilocraft.essentials.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import org.kilocraft.essentials.config.*;

public class ConfigHandler {
    private static String configPath = "^KiloEssentials^Config^".replace("^", File.separator);
    public static void handle() {
        ArrayList<String> configFiles = new ArrayList<String>(){{
            add("General.yml");
            add("Messages.yml");
            add("Ranks.yml");
        }};

        configFiles.forEach((config) -> {
            new ConfigFile(
                    config,
                    configPath,
                    "ConfigFiles",
                    false,
                    true
            );

            try {
                new KiloConfig(configPath, config);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        });



    }
}
