package org.kilocraft.essentials.craft.config;

import org.kilocraft.essentials.craft.KiloEssentials;
import org.kilocraft.essentials.craft.config.provided.ConfigProvided;

public class ConfigProvider {
    public ConfigProvider() {
        KiloEssentials.getLogger().info("Providing the configurations...");
    }

    public <C extends ConfigProvided> C provide(C config) {
        config.configValues().forEach((var) -> {
            try {
                config.getClass().getField(var).set(config, config.get(var.replace("_", ".")));
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        });

        return config;
    }
}
