package org.kilocraft.essentials.config_old;

import com.electronwill.nightconfig.core.file.FileConfig;
import org.kilocraft.essentials.KiloEssentialsImpl;
import org.kilocraft.essentials.config_old.provided.ConfigProvider;
import org.kilocraft.essentials.provided.KiloFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author  CODY_AI
 * @version 1.3
 */

public class KiloConfigOLD {

    private static List<ConfigIOProvider> callbacks = new ArrayList<>();
    private static String workingDir = System.getProperty("user.dir");
    private static String essentialsDir = "/KiloEssentials";
    private static String configPath = workingDir + essentialsDir + "/config/";
    private static String resourcePath = "assets/configurations_old/";
    private static String dataDir = essentialsDir + "/data/";

    private static HashMap<String, String> configFiles = new HashMap<String, String>(){{
        put("kilo_essentials.yml", workingDir + "/");
        put("messaes.yml", configPath);
        //put("commands.yml", configPath);
        put("help.yml", configPath);
        put("rules.yml", configPath);
        put("particle_types.yml", configPath);
    }};

    private static ConfigProvider provider;

    public KiloConfigOLD() {
        handle();
        provider = new ConfigProvider();

        KiloEssentialsImpl.getLogger().info("Configurations are now loaded");
    }

    static FileConfig MAIN = FileConfig.of(workingDir + "/kilo_essentials.yml");
    static FileConfig MESSAGES = FileConfig.of(configPath + "/messaes.yml");
    //static FileConfig COMMANDS = FileConfig.of(configPath + "commands.yml");

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

    public static <C extends ConfigIOProvider> void registerIOCallBaack(C callback) {
        callbacks.add(callback);
    }

    public static void triggerCallbacks() {
        for (ConfigIOProvider callback : callbacks) {
            callback.fromConfig(provider);
        }
    }

    public static void saveCallbacks() {
        for (ConfigIOProvider callback : callbacks) {
            callback.toConfig(provider);
        }
    }

    public static FileConfig getFileConfigOfMain() {
        return MAIN;
    }

    public static FileConfig getFileConfigOfMessages() {
        return MESSAGES;
    }

    public static FileConfig getFileConfigOfCommands() {
        return null;
        //return COMMANDS;
    }

    public static ConfigProvider getProvider() {
        return provider;
    }

    public static String getWorkingDirectory() {
        return workingDir;
    }

    public static String getConfigPath() {
        return configPath;
    }

    public static String getEssentialsDirectory() {
        return workingDir + essentialsDir;
    }

    public static String getDataDirectory() {
        return workingDir + dataDir;
    }

    public static String getMessage(String key, Object... objects) {
        return provider.getMessages().getMessage(key, objects);
    }

    public String getMessage(ConfigCache c, Object... objects) {
        return provider.getMessages().getMessage(c, objects);
    }

    public static void load() {
        MAIN.load();
        MESSAGES.load();
        //COMMANDS.load();
        ConfigCache.load();
    }

}