package org.kilocraft.essentials;

import net.fabricmc.api.DedicatedServerModInitializer;
import org.kilocraft.essentials.craft.ThreadManager;
import org.kilocraft.essentials.craft.threaded.ThreadedKiloEssentialsMod;

public class onInit implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        ThreadManager.setMainName("KiloEssentials");
        ThreadManager main = new ThreadManager(new ThreadedKiloEssentialsMod());

        main.start();
    }
}
