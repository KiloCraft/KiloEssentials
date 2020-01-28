package org.kilocraft.essentials;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.minecraft.SharedConstants;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.config.KiloConfig;

import java.io.File;

public class KiloEssentialsMod implements DedicatedServerModInitializer {
	@Override
    public void onInitializeServer() {
        File debugFile = new File(KiloConfig.getWorkingDirectory() + "/kiloessentials.debug");
        if (debugFile.exists()) {
            KiloEssentials.getLogger().warn("****[!] Alert: Server is running in development mode!");
            SharedConstants.isDevelopment = true;
        }

        new KiloEssentialsImpl(
                new KiloEvents(),
                new KiloConfig(),
                new KiloCommands());
    }
}
