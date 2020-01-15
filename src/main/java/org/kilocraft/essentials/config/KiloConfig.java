package org.kilocraft.essentials.config;

import com.electronwill.nightconfig.core.file.FileConfig;
import org.kilocraft.essentials.KiloEssentialsImpl;
import org.kilocraft.essentials.config.provided.ConfigProvider;
import org.kilocraft.essentials.provided.KiloFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author  CODY_AI
 * @version 1.3
 */

public class KiloConfig {

    private static List<ConfigIOProvider> callbacks = new ArrayList<>();
    private static String workingDir = System.getProperty("user.dir");
    private static String configPath = workingDir + "/KiloEssentials/config/";
    private static String resourcePath = "assets/configurations/";

    private static HashMap<String, String> configFiles = new HashMap<String, String>(){{
        put("KiloEssentials.yaml", workingDir + "/");
        put("Messages.yaml", configPath);
        //put("Commands.yaml", configPath);
        put("HelpMessage.yaml", configPath);
        put("Rules.yaml", configPath);
    }};

    private static ConfigProvider provider;

    public KiloConfig() {
        handle();
        provider = new ConfigProvider();

        KiloEssentialsImpl.getLogger().info("Configurations are now loaded");
    }

    static FileConfig MAIN = FileConfig.of(workingDir + "/KiloEssentials.yaml");
    static FileConfig MESSAGES = FileConfig.of(configPath + "/Messages.yaml");
    //static FileConfig COMMANDS = FileConfig.of(configPath + "Commands.yaml");

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