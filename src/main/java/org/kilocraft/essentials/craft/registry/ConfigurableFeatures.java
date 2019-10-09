package org.kilocraft.essentials.craft.registry;

import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.craft.KiloEssentials;
import org.kilocraft.essentials.craft.config.KiloConifg;

import java.util.List;

public class ConfigurableFeatures {

    public ConfigurableFeatures() {
        KiloEssentials.getLogger().info("Registering the Configurable features...");
    }

    public <F extends ConfigurableFeature> F tryToRegister(F feature, String configID) {
        boolean isEnabled = false;
        try {
            @Nullable List<String> list = KiloConifg.getConfigurableFeatures().get("configurableFeatures.");
            if (list.contains(configID)) isEnabled = true;
        } finally {
            if (isEnabled) register(feature);
        }

        return feature;
    }

    private <F extends ConfigurableFeature> void register(F feature) {
        feature.register();
    }

}
