package org.kilocraft.essentials.craft.threaded;

import org.kilocraft.essentials.craft.KiloCommands;
import org.kilocraft.essentials.craft.KiloEssentials;
import org.kilocraft.essentials.craft.KiloEvents;

public class ThreadedKiloEssentialsMod implements Runnable, KiloThread {
    @Override
    public String getName() {
        return "Main";
    }

    @Override
    public void run() {
        new KiloEssentials(
                new KiloEvents(),
                new KiloCommands()
        );
    }

}
