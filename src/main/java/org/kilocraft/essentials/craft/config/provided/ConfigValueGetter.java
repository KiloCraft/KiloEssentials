package org.kilocraft.essentials.craft.config.provided;

import com.electronwill.nightconfig.core.file.FileConfig;

public class ConfigValueGetter {
    private FileConfig config;
    private ProvidedValueReplaced valueReplaced;

    public ConfigValueGetter(FileConfig fileConfig) {
        this.config = fileConfig;
        valueReplaced = new ProvidedValueReplaced(this.config);
    }

    public String get(boolean allowGlobalObjects, String key) {
        return valueFormatter(allowGlobalObjects, key);
    }

    public String getFormatter(boolean allowGlobalObjects, String key, Object... objects) {
        return customValueFormatter(allowGlobalObjects, key, objects);
    }

    public <T> T getValue(String key) {
        return this.config.get(key);
    }

    public ProvidedValueReplaced getValueReplacer() {
        return valueReplaced;
    }

    private String valueFormatter(boolean allowGlobalObjects, String key) {
        String value = this.config.get(key);
        return allowGlobalObjects ? this.valueReplaced.replaceGlobalObjects(value) : value;
    }

    private String customValueFormatter(boolean allowGlobalObjects, String key, Object... objects) {
        String value = this.config.getOrElse(key, "NULL");
        String result = allowGlobalObjects ? this.valueReplaced.replaceGlobalObjects(value) : value;
        System.out.println(result);
        return String.format(result, objects);
    }


}
