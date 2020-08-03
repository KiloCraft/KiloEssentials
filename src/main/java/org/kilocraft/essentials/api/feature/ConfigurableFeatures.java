package org.kilocraft.essentials.api.feature;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.SharedConstants;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.KiloDebugUtils;
import org.kilocraft.essentials.KiloEssentialsImpl;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.config.KiloConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfigurableFeatures {
    private static final Map<ConfigurableFeature, String> features = Maps.newHashMap();
    private static final List<TickListener> tickListeners = Lists.newArrayList();
    private static ConfigurableFeatures INSTANCE;

    public ConfigurableFeatures() {
        INSTANCE = this;
        KiloEssentialsImpl.getLogger().info("Registering the Configurable Features...");
    }

    public <F extends ConfigurableFeature> boolean isEnabled(F feature) {
        return features.containsKey(feature);
    }

    private boolean isConfigEnabled(@NotNull final String configKey) {
        return KiloConfig.getMainNode().getNode("features").getNode(configKey).getBoolean();
    }

    public <F extends ConfigurableFeature> boolean register(@NotNull final F feature, @NotNull final String configKey) {
        if (!isConfigEnabled(configKey) && features.containsKey(feature)) {
            if (feature instanceof TickListener) {
                tickListeners.remove(feature);
            }

            features.remove(feature);
        }

        if (SharedConstants.isDevelopment) {
            KiloDebugUtils.getLogger().info("Initialing {}", feature.getClass().getName());
        }

        if (feature instanceof TickListener) {
            tickListeners.add((TickListener) feature);
        }

        features.put(feature, configKey);
        feature.register();
        return true;
    }

    public void loadAll(boolean reload) {
        for (Map.Entry<ConfigurableFeature, String> entry : features.entrySet()) {
            ConfigurableFeature feature = entry.getKey();
            String id = entry.getValue();
            if (isConfigEnabled(id)) {
                if (feature instanceof ReloadableConfigurableFeature) {
                    ((ReloadableConfigurableFeature) feature).load();
                    ((ReloadableConfigurableFeature) feature).load(reload);
                }
            } else {
                features.remove(feature);
                if (feature instanceof TickListener) {
                    tickListeners.remove(feature);
                }

                if (feature instanceof ReloadableConfigurableFeature) {
                    ((ReloadableConfigurableFeature) feature).unload();
                }
            }
        }
    }

    public void onTick() {
        for (TickListener listener : tickListeners) {
            try {
                listener.onTick();
            } catch (Exception e) {
                KiloEssentials.getLogger().fatal("An unexpected error occurred while processing a Tick Event", e);
            }
        }
    }

    public static ConfigurableFeatures getInstance() {
        return INSTANCE;
    }
}
