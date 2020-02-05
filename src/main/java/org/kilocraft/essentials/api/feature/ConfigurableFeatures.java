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
            if (KiloConfig.getMainNode().getNode("features").getNode(configID).getBoolean()) {
                if (SharedConstants.isDevelopment)
                    KiloEssentialsImpl.getLogger().info("Initialing \"" + feature.getClass().getName() + "\"");

                feature.register();
            }
        } catch (NullPointerException ignored) {
            //Don't enable the feature:: PASS
        }

        return feature;
    }
}
