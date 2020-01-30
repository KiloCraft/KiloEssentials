package org.kilocraft.essentials.config_old;

import org.kilocraft.essentials.config_old.provided.ConfigProvider;

public interface ConfigIOProvider {

    /**
     * Sets the values you want to the config file
     *
     * @param config - an instance of the ConfigProvider
     */
    void toConfig(ConfigProvider config);

    /**
     * Get the values you want from a config file
     *
     * @param config - an instance of the ConfigProvider
     */
    void fromConfig(ConfigProvider config);

}
