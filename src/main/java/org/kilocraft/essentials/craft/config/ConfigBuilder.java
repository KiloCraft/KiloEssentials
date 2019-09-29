package org.kilocraft.essentials.craft.config;

import com.electronwill.nightconfig.core.file.FileConfig;

import java.util.HashMap;

public class ConfigBuilder {
    private HashMap<String, Object> hashMap = new HashMap<>();
    private FileConfig config;

    public ConfigBuilder(FileConfig fileConfig) {
        this.config = fileConfig;
    }

    public void addValue(String path, Object value) {
        hashMap.put(path, value);
    }

    public void build() {
        this.config.load();
        try {
            hashMap.forEach((key, value) -> {
                this.config.add(key, value);
            });
        } finally {
            this.config.save();
            this.config.close();
        }
    }


}
