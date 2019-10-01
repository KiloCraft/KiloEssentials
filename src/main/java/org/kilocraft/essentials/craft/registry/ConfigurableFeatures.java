package org.kilocraft.essentials.craft.registry;

import com.electronwill.nightconfig.core.file.FileConfig;
import org.kilocraft.essentials.craft.KiloEssentials;
import org.kilocraft.essentials.craft.config.KiloConifg;

import java.util.HashMap;

public class ConfigurableFeatures {
    private FileConfig config = FileConfig.of("");
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
        boolean isEnabled = false;
        try {
            if (config.get("ConfigurableFeatures." + configID) == null) {

//                ConfigBuilder configBuilder = new ConfigBuilder(KiloEssentials.INSTANCE.getConfig().getMain());
//                configBuilder.addValue("ConfigurableFeatures." + configID, true);
//                configBuilder.build();

                if (KiloConifg.getMain().get("ConfigurableFeatures." + configID)) {
                    register(f);
                }
            }
        } finally {
            if (isEnabled) register(f);
        }

        return f;
    }

    private <C extends ConfigurableFeature> void register(C c) {
        c.register();
    }

}
