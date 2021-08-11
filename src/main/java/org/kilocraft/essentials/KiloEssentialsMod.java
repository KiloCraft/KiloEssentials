package org.kilocraft.essentials;

import net.fabricmc.api.DedicatedServerModInitializer;
import org.kilocraft.essentials.api.KiloEssentials;

public class KiloEssentialsMod implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        KiloEssentials essentials = new KiloEssentials();
    }

}
