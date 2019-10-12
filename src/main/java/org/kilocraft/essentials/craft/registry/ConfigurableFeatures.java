package org.kilocraft.essentials.craft.registry;

import org.kilocraft.essentials.craft.KiloEssentials;
import org.kilocraft.essentials.craft.config.KiloConifg;

public class ConfigurableFeatures {
    public ConfigurableFeatures() {
        KiloEssentials.getLogger().info("Registering the Configurable features...");
    }

    public <F extends ConfigurableFeature> F tryToRegister(F feature, String configID) {
        try {
            if (KiloConifg.getMain().getOrElse("features." + configID, false)) {
                KiloEssentials.getLogger().info("Initialing \"" + configID + "\"");
                feature.register();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            //Don't enable the feature:: PASS
        }

        return feature;
    }
}
