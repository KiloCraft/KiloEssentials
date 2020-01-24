package org.kilocraft.essentials.api.feature;

import net.minecraft.SharedConstants;
import org.kilocraft.essentials.KiloEssentialsImpl;
import org.kilocraft.essentials.config.KiloConfig;

public class ConfigurableFeatures {
    public ConfigurableFeatures() {
        KiloEssentialsImpl.getLogger().info("Registering the Configurable Features...");
    }

    public <F extends ConfigurableFeature> F tryToRegister(F feature, String configID) {
        try {
            if (KiloConfig.getFileConfigOfMain().getOrElse("features." + configID, false)) {
                if (SharedConstants.isDevelopment)
                    KiloEssentialsImpl.getLogger().info("Initialing \"" + feature.getClass().getName() + "\"");
                else
                    KiloEssentialsImpl.getLogger().info("Initialing \"" + configID + "\"");

                feature.register();
            }
        } catch (NullPointerException ignored) {
            //Don't enable the feature:: PASS
        }

        return feature;
    }
}
