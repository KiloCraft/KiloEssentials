package org.kilocraft.essentials.craft.config;

import org.kilocraft.essentials.api.config.ConfigFile;

import java.io.File;

public enum Configs {
    GENERAL("General"),
    MESSAGES("Messages"),
    RANKS("Ranks");

    private final String format = "yml";
    private String name;
    private File file;

    Configs(String name) {
        this.name = name;
        this.file = new File(getConfigPath() + name + "." + format);
    }

    public static String getConfigPath() {
        return ConfigFile.currentDir + "^KiloEssentials^Config^".replace("^", File.separator);
    }

    public String getName() {
        return this.name;
    }

    public File getFile() {
        return this.file;
    }

}
