package org.kilocraft.essentials;

import net.fabricmc.api.DedicatedServerModInitializer;
import org.kilocraft.essentials.craft.registry.ConfigurableFeatures;
import org.kilocraft.essentials.craft.KiloCommands;
import org.kilocraft.essentials.craft.KiloEssentials;
import org.kilocraft.essentials.craft.KiloEvents;
import org.kilocraft.essentials.craft.config.DataHandler;
import org.kilocraft.essentials.craft.config.KiloConfig;

public class onInit implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        new KiloEssentials(
                new KiloConfig(),
                new KiloEvents(),
                new KiloCommands(),
                new DataHandler(),
                new ConfigurableFeatures()
        );
    }
}
