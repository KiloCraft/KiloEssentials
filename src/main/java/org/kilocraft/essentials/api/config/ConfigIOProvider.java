package org.kilocraft.essentials.api.config;

import org.kilocraft.essentials.config.provided.ConfigProvider;

public interface ConfigIOProvider {

    /**
     * Sets the values you want to the config file
     *
     * @param config - an instance of the ConfigProvider
     */
    void toConifg(ConfigProvider config);

    /**
     * Get the values you want from a config file
     *
     * @param config - an instance of the ConfigProvider
     */
    void fromConfig(ConfigProvider config);

}
