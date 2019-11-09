package org.kilocraft.essentials;

import net.fabricmc.api.DedicatedServerModInitializer;
import org.kilocraft.essentials.threaded.ThreadedKiloEssentialsMod;

public class KiloEssentialsMod implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        System.out.println("Started");
        ThreadManager.setMainName("KiloEssentials");
        ThreadManager mainThread = new ThreadManager(new ThreadedKiloEssentialsMod());
        mainThread.setMainThread(true);
        mainThread.start();
    }
}
