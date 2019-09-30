package org.kilocraft.essentials;

import net.fabricmc.api.DedicatedServerModInitializer;
import org.kilocraft.essentials.craft.threaded.ThreadedKiloEssentialsMod;

public class onInit implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
//        new KiloEssentials(
//                new KiloConfig(),
//                new KiloEvents(),
//                new KiloCommands(),
//                new DataHandler(),
//                new ConfigurableFeatures()
//        );

        ThreadedKiloEssentialsMod mod = new ThreadedKiloEssentialsMod("KiloEssentials");
        mod.start();
        mod.run();
    }
}
