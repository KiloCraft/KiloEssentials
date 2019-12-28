package org.kilocraft.essentials.threaded;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.KiloEssentialsImpl;
import org.kilocraft.essentials.KiloEvents;
import org.kilocraft.essentials.config.KiloConfig;

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
        new KiloEssentialsImpl(
                new KiloEvents(),
                new KiloConfig(),
                new KiloCommands()
        );
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

}
