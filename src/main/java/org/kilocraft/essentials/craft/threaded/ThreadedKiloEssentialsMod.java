package org.kilocraft.essentials.craft.threaded;

import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.craft.KiloCommands;
import org.kilocraft.essentials.craft.KiloEssentials;
import org.kilocraft.essentials.craft.KiloEvents;
import org.kilocraft.essentials.craft.ThreadManager;
import org.kilocraft.essentials.craft.config.DataHandler;

public class ThreadedKiloEssentialsMod implements Runnable, KiloThread {
    private Logger logger;

    public ThreadedKiloEssentialsMod() {
    }

    @Override
    public void run() {
        new KiloEssentials(
                new ThreadManager("KiloEssentials"),
                new KiloEvents(),
                new KiloCommands(),
                new DataHandler()
        );
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

}
