package org.kilocraft.essentials.craft.config.provided;

import com.electronwill.nightconfig.core.file.FileConfig;

import java.util.List;

public interface ConfigProvided {
    /**
     * A list of the config value keys
     * @return List of strings
     */
    List<String> configValues();

    /**
     * The FileConfig you use to get the values
     * @return FileConfig
     */
    FileConfig config();

    /**
     * The method to get a value from a key if its not in the list
     *
     * @param key the value's path, each part separated by a dot. Example "a.b.c"
     * @param <T>  the value's type
     * @return the value at the given path, or {@code null} if there is no such value.
     */
    default <T> T get(String key) {
        return config().get(key);
    }
}
