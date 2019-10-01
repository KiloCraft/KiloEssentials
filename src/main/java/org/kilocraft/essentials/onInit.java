package org.kilocraft.essentials;

import net.fabricmc.api.DedicatedServerModInitializer;
import org.kilocraft.essentials.craft.threaded.ThreadedKiloEssentialsMod;

public class onInit implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        Thread thread = new Thread(new ThreadedKiloEssentialsMod());
        thread.setName("KiloEssentials-MAIN");
        thread.start();
    }
}
