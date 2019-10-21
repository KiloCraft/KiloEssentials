package org.kilocraft.essentials.craft.threaded;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.craft.KiloCommands;
import org.kilocraft.essentials.craft.KiloEssentials;
import org.kilocraft.essentials.craft.KiloEvents;

public class ThreadedKiloEssentialsMod implements Runnable, KiloThread {
    private Logger logger;

    public ThreadedKiloEssentialsMod() {
        logger = LogManager.getFormatterLogger(getName());
    }

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

    @Override
    public Logger getLogger() {
        return logger;
    }

}
