package org.kilocraft.essentials.craft.threaded;

import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.craft.KiloCommands;
import org.kilocraft.essentials.craft.KiloEssentials;
import org.kilocraft.essentials.craft.KiloEvents;
import org.kilocraft.essentials.craft.data.KiloData;

public class ThreadedKiloEssentialsMod implements Runnable, KiloThread {
    private Logger logger;

    public ThreadedKiloEssentialsMod() {
    }

    @Override
    public String getName() {
        return "Main";
    }

    @Override
    public void run() {
        new KiloEssentials(
                new KiloEvents(),
                new KiloCommands(),
                new KiloData()
        );
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

}
