package org.kilocraft.essentials;

import net.fabricmc.api.DedicatedServerModInitializer;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.config.KiloConfig;

public class KiloEssentialsMod implements DedicatedServerModInitializer {
	@Override
    public void onInitializeServer() {
    }

    public static void setup() {
        new ModConstants().loadConstants();
        KiloConfig.load();
        new KiloCommands();
        if (!KiloEssentialsImpl.isRunning()) {
            new KiloEssentialsImpl();
        }
    }
}
