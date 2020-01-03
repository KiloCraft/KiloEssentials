package org.kilocraft.essentials.config;

import com.electronwill.nightconfig.core.file.FileConfig;
import org.kilocraft.essentials.config.variablehelper.LocalConfigVariable;
import org.kilocraft.essentials.config.variablehelper.LocalVariableReplaced;
import org.kilocraft.essentials.config.variablehelper.ProvidedValueReplaced;

public class ConfigValueGetter {
    private FileConfig config;
    private ProvidedValueReplaced valueReplaced;
    private LocalVariableReplaced localReplaced;

    public ConfigValueGetter(FileConfig fileConfig) {
        this.config = fileConfig;
        this.valueReplaced = new ProvidedValueReplaced(this.config);
        localReplaced = new LocalVariableReplaced();
    }

    public String get(boolean allowGlobalObjects, String key) {
        return valueFormatter(allowGlobalObjects, key);
    }

    public String get(boolean allowGlobalObjects, ConfigCache c) {
        return allowGlobalObjects ? this.valueReplaced.replaceGlobalObjects(String.valueOf(c.getValue())) : String.valueOf(c.getValue());
    }

    public String getMessage(String key, Object... objects) {
        String string = valueFormatter(true, key);
        return (objects != null) ? String.format(string, objects) : string;
    }

    public String getMessage(ConfigCache c, Object... objects) {
        String string = this.valueReplaced.replaceGlobalObjects(String.valueOf(c.getValue()));
        return (objects != null) ? String.format(string, objects) : string;
    }

    public String getFormatter(boolean allowGlobalObjects, String key, Object... objects) {
        return customValueFormatter(allowGlobalObjects, key, objects);
    }

    public <L extends LocalConfigVariable> String getLocal(boolean replaceLocal, String key, L localFormatter) {
        return replaceLocal ? valueWithLocalFormat(key, localFormatter) : this.config.getOrElse(key, "NULL");
    }

    public <L extends LocalConfigVariable> String getLocalFormatter(boolean replaceLocal, String key, L localFormatter, Object... objects) {
        String string = replaceLocal ? valueWithLocalFormat(key, localFormatter) : this.config.getOrElse(key, "NULL");
        return String.format(string, objects);
    }

    @SafeVarargs
    public final <L extends LocalConfigVariable> String getLocalFormatter(String key, L... localFormatters) {
        return this.localReplaced.replace(getValue(key), localFormatters);
    }

    public <T> T getValue(String key) {
        return this.config.get(key);
    }

    public Boolean getBooleanSafely(String key, boolean defaultValue) {
        return (boolean) this.config.getOrElse(key, defaultValue);
    }

    public Boolean getBooleanSafely(ConfigCache c, boolean defaultValue) {
        return c.getValue().equals("NULL") ? defaultValue : (Boolean) c.getValue();
    }

    public Integer getIntegerSafely(String key, int defaultValue) {
        return (int) this.config.getOrElse(key, defaultValue);
    }

    public Long getLongSafely(String key, long defaultValue) {
        return this.config.getLongOrElse(key, defaultValue);
    }

    public Float getFloatSafely(String key, String defaultValue) {
        return Float.parseFloat(this.config.getOrElse(key, defaultValue));
    }

    public String getStringSafely(String key, String defaultValue) {
        return this.config.getOrElse(key, defaultValue);
    }

    public String getStringSafely(ConfigCache c, String defaultValue) {
        return c.getValue().equals("NULL") ? defaultValue : String.valueOf(c.getValue());
    }

    public ProvidedValueReplaced getValueReplacer() {
        return valueReplaced;
    }

    public LocalVariableReplaced getLocalReplacer() {
        return localReplaced;
    }

    private String valueFormatter(boolean allowGlobalObjects, String key) {
        String value = this.config.get(key);
        return allowGlobalObjects ? this.valueReplaced.replaceGlobalObjects(value) : value;
    }

    private String customValueFormatter(boolean allowGlobalObjects, String key, Object... objects) {
        String value = this.config.getOrElse(key, "NULL");
        String result = allowGlobalObjects ? this.valueReplaced.replaceGlobalObjects(value) : value;
        return String.format(result, objects);
    }

    private <L extends LocalConfigVariable> String valueWithLocalFormat(String key, L formatter) {
        return localReplaced.replace(this.config.getOrElse(key, "NULL"), formatter, this.config);
    }


}
