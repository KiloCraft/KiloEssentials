package org.kilocraft.essentials;

import net.fabricmc.api.DedicatedServerModInitializer;
import org.kilocraft.essentials.config.KiloConfig;

public class KiloEssentialsMod implements DedicatedServerModInitializer {

	@Override
    public void onInitializeServer() {
        new KiloEssentialsImpl(
                new KiloEvents(),
                new KiloConfig(),
                new KiloCommands());
    }
}
