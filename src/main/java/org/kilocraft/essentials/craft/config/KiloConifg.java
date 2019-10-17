package org.kilocraft.essentials.craft.config;

import com.electronwill.nightconfig.core.file.FileConfig;
import org.kilocraft.essentials.craft.KiloEssentials;
import org.kilocraft.essentials.craft.config.provided.MainConfig;
import org.kilocraft.essentials.craft.config.provided.MessagesConfig;
import org.kilocraft.essentials.craft.provider.KiloFile;

import java.util.HashMap;

/**
 * @author  CODY_AI
 * @version 1.2
 */

public class KiloConifg {

    private static String workingDir = System.getProperty("user.dir");
    private static String configPath = workingDir + "/KiloEssentials/config/";
    private static String resourcePath = "assets/configurations/";

    private static HashMap<String, String> configFiles = new HashMap<String, String>(){{
        put("KiloEssentials.yaml", workingDir + "/");
        put("Messages.yaml", configPath);
    }};

    public KiloConifg() {
        handle();
        ConfigProvider configProvider = new ConfigProvider();
        configProvider.provide(new MainConfig());

        KiloEssentials.getLogger().info("Configurations are now loaded");
    }

    private static MainConfig mainConfig;
    private static MessagesConfig messagesConfig;

    static FileConfig MAIN = FileConfig.of(workingDir + "/KiloEssentials.yaml");
    static FileConfig MESSAGES = FileConfig.of(configPath + "/Messages.yaml");

    private void handle() {
        try {
            configFiles.forEach((name, path) -> {
                KiloFile file = new KiloFile(name, path);
                file.tryToLoad(resourcePath + name);
            });

            load();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static FileConfig getFileConfigOfMain() {
        return MAIN;
    }

    public static FileConfig getFileConfigOfMessages() {
        return MESSAGES;
    }

    public static MainConfig getMain() {
        return mainConfig;
    }

    public static String getWorkingDirectory() {
        return workingDir;
    }

    public static void load() {
        MAIN.load();
        MESSAGES.load();
    }

}