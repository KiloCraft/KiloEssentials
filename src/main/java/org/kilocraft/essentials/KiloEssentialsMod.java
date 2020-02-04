package org.kilocraft.essentials;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.minecraft.SharedConstants;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.config.main.Config;
import org.kilocraft.essentials.config.KiloConfigurate;
import org.kilocraft.essentials.config_old.KiloConfig;

import java.io.File;
import java.util.Arrays;

public class KiloEssentialsMod implements DedicatedServerModInitializer {
	@Override
    public void onInitializeServer() {
        File debugFile = new File(KiloConfig.getWorkingDirectory() + "/kiloessentials.debug");
        if (debugFile.exists()) {
            KiloEssentials.getServer().getLogger().warn("**** SERVER IS RUNNING IN DEBUG/DEVELOPMENT MODE!");
            SharedConstants.isDevelopment = true;
        }

        new KiloConfigurate();
        new KiloEssentialsImpl(new KiloEvents(), new KiloConfig());


        Config config = KiloConfigurate.main();
        System.out.println(Arrays.toString(config.playerList().footer.toArray()));


    }
}
