package org.kilocraft.essentials.craft.config;

import com.electronwill.nightconfig.core.file.FileConfig;
import org.kilocraft.essentials.craft.KiloEssentials;
import org.kilocraft.essentials.craft.data.KiloData;
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
        put("Warps.yaml", KiloData.getDataDirectoryPath());
    }};

    public KiloConifg() {
        handle();
        KiloEssentials.getLogger().info("Configurations are now loaded");
    }

    static FileConfig MAIN = FileConfig.of(workingDir + "/KiloEssentials.yaml");
    static FileConfig MESSAGES = FileConfig.of(configPath + "/Messages.yaml");
    //static FileConfig WARPS = FileConfig.of(configPath + "/Warps.yaml");

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


    public static FileConfig getMain() {
        return MAIN;
    }

    public static FileConfig getMessages() {
        return MESSAGES;
    }

//    public static FileConfig getWarps() {
//        return WARPS;
//    }

    public static String getWorkingDirectory() {
        return workingDir;
    }

    public static void load() {
        MAIN.load();
        MESSAGES.load();
        //WARPS.load();
    }

}