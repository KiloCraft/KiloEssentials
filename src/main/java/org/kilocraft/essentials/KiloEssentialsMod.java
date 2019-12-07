package org.kilocraft.essentials;

import net.fabricmc.api.DedicatedServerModInitializer;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.threaded.ThreadedKiloEssentialsMod;

public class KiloEssentialsMod implements DedicatedServerModInitializer {

	@Override
    public void onInitializeServer() {
        ModConstants.getLogger().info("Initializing KiloEssentials...");
        ThreadManager.setMainName("KiloEssentials");
        ThreadManager mainThread = new ThreadManager(new ThreadedKiloEssentialsMod());
        mainThread.setMainThread(true);
        mainThread.start();
    }
}
