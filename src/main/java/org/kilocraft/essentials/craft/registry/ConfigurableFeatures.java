package org.kilocraft.essentials.craft.registry;

import com.electronwill.nightconfig.core.file.FileConfig;
import org.kilocraft.essentials.craft.KiloEssentials;
import org.kilocraft.essentials.craft.config.ConfigBuilder;
import org.kilocraft.essentials.craft.config.KiloConfig;

import java.util.HashMap;

public class ConfigurableFeatures {
    private FileConfig config = FileConfig.of(KiloConfig.getGeneral().getFile().getAbsolutePath());
    private HashMap<String, Object> hashMap = new HashMap<>();

    public ConfigurableFeatures() {
        KiloEssentials.getLogger().info("Registering the Configurable features...");
    }

    public void registerFeatures() {
    }

    private void addToConfig() {

    }

    public void close() {
        config.close();
    }

    public HashMap<String, Object> getFeaturesMap() {
        return hashMap;
    }

    public <F extends ConfigurableFeature> F tryToRegister(F f, String configID) {
        config.load();
        boolean isEnabled = false;
        try {
            if (config.get("ConfigurableFeatures." + configID) == null) {

                ConfigBuilder configBuilder = new ConfigBuilder(KiloConfig.getGeneral());
                configBuilder.addValue("ConfigurableFeatures." + configID, true);
                configBuilder.build();

                if (KiloConfig.getGeneral().get("ConfigurableFeatures." + configID)) {
                    register(f);
                }
            }
        } finally {
            if (isEnabled) register(f);
        }

        config.close();
        return f;
    }

    private <C extends ConfigurableFeature> void register(C c) {
        c.register();
    }

}
