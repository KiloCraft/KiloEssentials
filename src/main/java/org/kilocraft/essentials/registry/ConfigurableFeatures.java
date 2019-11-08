package org.kilocraft.essentials.registry;

import org.kilocraft.essentials.KiloEssentials;
import org.kilocraft.essentials.config.KiloConifg;

public class ConfigurableFeatures {
    public ConfigurableFeatures() {
        KiloEssentials.getLogger().info("Registering the Configurable features...");
    }

    public <F extends ConfigurableFeature> F tryToRegister(F feature, String configID) {
        try {
            if (KiloConifg.getFileConfigOfMain().getOrElse("features." + configID, false)) {
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
