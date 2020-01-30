package org.kilocraft.essentials;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.minecraft.SharedConstants;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.config_old.KiloConfigOLD;

import java.io.File;

public class KiloEssentialsMod implements DedicatedServerModInitializer {
	@Override
    public void onInitializeServer() {
        File debugFile = new File(KiloConfigOLD.getWorkingDirectory() + "/kiloessentials.debug");
        if (debugFile.exists()) {
            KiloEssentials.getLogger().warn("**** SERVER IS RUNNING IN DEBUG/DEVELOPMENT MODE!");
            SharedConstants.isDevelopment = true;
        }

        new KiloEssentialsImpl(
                new KiloEvents(),
                new KiloConfigOLD(),
                new KiloCommands());
    }
}
