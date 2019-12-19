package org.kilocraft.essentials;

import net.fabricmc.api.DedicatedServerModInitializer;

public class KiloEssentialsMod implements DedicatedServerModInitializer {

	@Override
    public void onInitializeServer() {
        new KiloEssentialsImpl(
                new KiloEvents(),
                new KiloCommands()
        );

        //TODO: Clean this up
//        ModConstants.getLogger().info("Initializing KiloEssentials...");
//        ThreadManager.setMainName("KiloEssentials");
//        ThreadManager mainThread = new ThreadManager(new ThreadedKiloEssentialsMod());
//        mainThread.setMainThread(true);
//        mainThread.start();
    }
}
