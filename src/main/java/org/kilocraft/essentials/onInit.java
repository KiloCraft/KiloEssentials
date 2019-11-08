package org.kilocraft.essentials;

import net.fabricmc.api.DedicatedServerModInitializer;
import org.kilocraft.essentials.threaded.ThreadedKiloEssentialsMod;

public class onInit implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        ThreadManager.setMainName("KiloEssentials");
        ThreadManager mainThread = new ThreadManager(new ThreadedKiloEssentialsMod());
        mainThread.setMainThread(true);
        mainThread.start();
    }
}
