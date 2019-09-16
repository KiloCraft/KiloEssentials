package org.kilocraft.essentials.craft.config;

import com.electronwill.nightconfig.core.file.FileConfig;

import java.io.File;

public class ConfigProvider {
    private static FileConfig config$General;
    private static FileConfig config$Messages;
    private static FileConfig config$Ranks;
    private static FileConfig config$Database;

    public static void provide(File... file) {
        for (int i = 0; i < file.length; i++) {
            switch (file[i].getName()) {
                case "General.yml":
                    config$General = FileConfig.of(file[i]);
                case "Messages.yml":
                    config$Messages = FileConfig.of(file[i]);
                case "Ranks.yml":
                    config$Ranks = FileConfig.of(file[i]);
                case "Database.yml":
                    config$Database = FileConfig.of(file[i]);

                break;
            }
        }
    }

    protected static void loadAll() {
        config$General.load();
        config$Messages.load();
        config$Ranks.load();
        config$Database.load();
    }

    protected static void saveAll() {
        config$General.save();
        config$Messages.save();
        config$Ranks.save();
        config$Database.save();
    }

    protected static void closeAll() {
        config$General.close();
        config$Messages.close();
        config$Ranks.close();
        config$Database.close();
    }

    protected static void reloadAll() {
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

    protected static FileConfig getConfig$Database() {
        return config$Database;
    }

}
