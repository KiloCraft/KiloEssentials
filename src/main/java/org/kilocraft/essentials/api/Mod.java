package org.kilocraft.essentials.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.craft.KiloCommands;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class Mod {
    public static Logger getLogger = LogManager.getFormatterLogger();
    public static Properties properties = new Properties();
    public static Properties lang = new Properties();

    private static String version;

    public Mod() {
        try {
            properties.load(Mod.class.getClassLoader().getResourceAsStream("info.properties"));
            lang.load(Mod.class.getClassLoader().getResourceAsStream("assets" + File.separator + "Lang.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        KiloCommands.register(true);
    }

    public static String getVersion() {
        return properties.getProperty("version");
    }

    public static String getMappingsVersion() {
        return properties.getProperty("fabric_yarn_mappings");
    }

    public static String getLoaderVersion() {
        return properties.getProperty("fabric_loader_version");
    }

}
