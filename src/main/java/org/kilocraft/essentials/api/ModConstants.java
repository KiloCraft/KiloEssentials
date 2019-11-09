package org.kilocraft.essentials.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Properties;

public class ModConstants {
    private static Logger logger = LogManager.getFormatterLogger();
    private static Properties properties = new Properties();
    private static Properties lang = new Properties(); // TODO i509VCB: Move lang stuff out of this later.

    public ModConstants() {
        try {
            properties.load(ModConstants.class.getClassLoader().getResourceAsStream("mod.properties"));
            lang.load(ModConstants.class.getClassLoader().getResourceAsStream("assets/Lang.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static Logger getLogger() {
        return logger;
    }

    public static Properties getProperties() {
        return properties;
    }

    public static Properties getLang() {
        return lang;
    }

    public static String getVersion() {
        return properties.getProperty("version");
    }

    public static String getVersionInt() {
        return properties.getProperty("version_int");
    }

    public static String getMappingsVersion() {
        return properties.getProperty("fabric_yarn_mappings");
    }

    public static String getLoaderVersion() {
        return properties.getProperty("fabric_loader_version");
    }

    public static String getMinecraftVersion() {
        return getProperties().getProperty("minecraft_version");
    }

    public static String getGitHash() {
        return properties.getProperty("git_hash");
    }

    public static String getGitHashFull() {
        return properties.getProperty("git_hash_full");
    }

    public static String getGitBranch() {
        return properties.getProperty("git_branch");
    }

    public static String getBuildType() {
        return properties.getProperty("build_type");
    }
}
