package org.kilocraft.essentials.craft.config;

import com.electronwill.nightconfig.core.file.FileConfig;
import org.kilocraft.essentials.api.Mod;

public class KiloConfig {
    private static final double schemaVersion = 1.0;

    public KiloConfig() {
        ConfigHandler.handle();
        load();
    }

    public static void load() {
        ConfigProvider.loadAll();
        Mod.getLogger().info("Loaded KiloConfig, version: " + KiloConfig.getConfigVersion());
    }

    public static void reload() {
        ConfigProvider.reloadAll();
    }

    public static void save() {
        ConfigProvider.saveAll();
    }

    public static void close() {
        ConfigProvider.closeAll();
    }

    public static FileConfig getGeneral() {
        return ConfigProvider.getConfig$General();
    }

    public static FileConfig getMessages() {
        return ConfigProvider.getConfig$Messages();
    }

    public static FileConfig getRanks() {
        return ConfigProvider.getConfig$Ranks();
    }

    public static FileConfig getDatabase() {
        return ConfigProvider.getConfig$Database();
    }

    public static double getConfigVersion() {
        return schemaVersion;
    }

}
