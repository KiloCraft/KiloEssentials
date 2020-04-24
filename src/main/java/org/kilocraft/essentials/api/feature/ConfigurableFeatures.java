package org.kilocraft.essentials.api.feature;

import net.minecraft.SharedConstants;
import org.kilocraft.essentials.KiloEssentialsImpl;
import org.kilocraft.essentials.config.KiloConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigurableFeatures {
    private static List<ConfigurableFeature> features = new ArrayList<>();;
    private static List<TickListener> tickListeners = new ArrayList<>();
    public ConfigurableFeatures() {
        KiloEssentialsImpl.getLogger().info("Registering the Configurable Features...");
    }

    public <F extends ConfigurableFeature> void tryToRegister(F feature, String configKey) {
        try {
            if (KiloConfig.getMainNode().getNode("features").getNode(configKey).getBoolean()) {
                if (SharedConstants.isDevelopment) {
                    KiloEssentialsImpl.getLogger().info("Initialing \"" + feature.getClass().getName() + "\"");
                }

                if (feature instanceof TickListener) {
                    tickListeners.add((TickListener) feature);
                } else {
                    features.add(feature);
                }

                feature.register();
            }
        } catch (NullPointerException ignored) {
            //Don't enable the feature:: PASS
        }

    }

    public void loadAll() {
        for (ConfigurableFeature feature : features) {
            if (feature instanceof RelodableConfigurableFeature) {
                ((RelodableConfigurableFeature) feature).load();
            }
        }
    }

    public void onTick() {
        for (TickListener listener : tickListeners) {
            try {
                listener.onTick();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
