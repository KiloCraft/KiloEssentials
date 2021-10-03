package org.kilocraft.essentials.config;

import java.util.Locale;

public class ConfigObjectReplacerUtil {
    private boolean isLocal;
    private String prefix;
    private String text;

    public ConfigObjectReplacerUtil(String prefix, String str) {
        this.prefix = prefix;
        this.text = str;
    }

    public ConfigObjectReplacerUtil(String prefix, String str, boolean isLocal) {
        this.prefix = prefix;
        this.text = str;
        this.isLocal = isLocal;
    }

    public String toVar(String key) {
        String s = this.prefix.toLowerCase(Locale.ROOT) + "_" + key.toLowerCase(Locale.ROOT);
        return this.isLocal ? "{" + s + "}" : "%" + s + "%";
    }

    public ConfigObjectReplacerUtil append(String key, Object value) {
        this.text = this.text.replace(this.toVar(key), String.valueOf(value));
        return this;
    }

    public String toString() {
        String s = this.text;
        this.text = null;
        this.prefix = null;
        return s;
    }
}
