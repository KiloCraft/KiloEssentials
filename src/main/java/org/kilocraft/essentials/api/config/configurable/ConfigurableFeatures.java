package org.kilocraft.essentials.api.config.configurable;

import org.kilocraft.essentials.KiloEssentialsImpl;
import org.kilocraft.essentials.config.KiloConfig;

public class ConfigurableFeatures {
    public ConfigurableFeatures() {
        KiloEssentialsImpl.getLogger().info("Registering the Configurable features...");
    }

    public <F extends ConfigurableFeature> F tryToRegister(F feature, String configID) {
        try {
            if (KiloConfig.getFileConfigOfMain().getOrElse("features." + configID, false)) {
                KiloEssentialsImpl.getLogger().info("Initialing \"" + configID + "\"");
                feature.register();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            //Don't enable the feature:: PASS
        }

        return feature;
    }
}
