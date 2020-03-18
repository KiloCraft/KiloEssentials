package org.kilocraft.essentials.api.feature;

import net.minecraft.SharedConstants;
import org.kilocraft.essentials.KiloEssentialsImpl;
import org.kilocraft.essentials.config.KiloConfig;

import java.util.ArrayList;
import java.util.List;

public class ConfigurableFeatures {
    private static List<ConfigurableFeature> features = new ArrayList<>();;
    public ConfigurableFeatures() {
        KiloEssentialsImpl.getLogger().info("Registering the Configurable Features...");
    }

    public <F extends ConfigurableFeature> void tryToRegister(F feature, String configKey) {
        try {
            if (KiloConfig.getMainNode().getNode("features").getNode(configKey).getBoolean()) {
                if (SharedConstants.isDevelopment)
                    KiloEssentialsImpl.getLogger().info("Initialing \"" + feature.getClass().getName() + "\"");

                features.add(feature);
                feature.register();
            }
        } catch (NullPointerException ignored) {
            //Don't enable the feature:: PASS
        }

    }

    public void loadAll() {
        for (ConfigurableFeature feature : features) {
            if (feature instanceof ReloadableConfigurableFeature) {
                ((ReloadableConfigurableFeature) feature).load();
            }
        }
    }
}
