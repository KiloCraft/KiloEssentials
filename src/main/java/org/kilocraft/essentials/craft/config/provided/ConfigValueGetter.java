package org.kilocraft.essentials.craft.config.provided;

import com.electronwill.nightconfig.core.file.FileConfig;

public class ConfigValueGetter {
    private FileConfig config;

    public ConfigValueGetter(FileConfig fileConfig) {
        this.config = fileConfig;
    }

    private String valueFormatter(boolean allowGlobalObjects, String key) {
        String value = this.config.get(key);


        return "";
    }

    private String customValueFormatter(boolean allowGlobalObjects, String key, Object... objects) {

        return "";
    }

    private <T> T getValue(String key) {
        return this.config.get(key);
    }

}
