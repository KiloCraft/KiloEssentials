package org.kilocraft.essentials.craft.config;

import com.electronwill.nightconfig.core.file.FileConfig;

import java.io.File;

public class ConfigProvider {
    private static FileConfig config$General;
    private static FileConfig config$Messages;
    private static FileConfig config$Ranks;

    public static void provide(File... file) {
        for (int i = 0; i < file.length; i++) {
            switch (file[i].getName()) {
                case "General.yml":
                    config$General = FileConfig.of(file[i]);
                    config$General.load();
                case "Messages.yml":
                    config$Messages = FileConfig.of(file[i]);
                case "Ranks.yml":
                    config$Ranks = FileConfig.of(file[i]);
                break;
            }
        }
    }

    public static void loadAll() {
        config$General.load();
        config$Messages.load();
        config$Ranks.load();
    }

    public static void saveAll() {
        config$General.save();
        config$Messages.save();
        config$Ranks.save();
    }

    public static void closeAll() {
        config$General.close();
        config$Messages.close();
        config$Ranks.close();
    }

    public static void reloadAll() {
        closeAll();
        loadAll();
    }

    protected static FileConfig getConfig$General() {
        return config$General;
    }

    protected static FileConfig getConfig$Messages() {
        return config$Messages;
    }

    protected static FileConfig getConfig$Ranks() {
        return config$Ranks;
    }


}
