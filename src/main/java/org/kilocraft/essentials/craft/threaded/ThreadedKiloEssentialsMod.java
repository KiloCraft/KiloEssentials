package org.kilocraft.essentials.craft.threaded;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.craft.KiloCommands;
import org.kilocraft.essentials.craft.KiloEssentials;
import org.kilocraft.essentials.craft.KiloEvents;
import org.kilocraft.essentials.craft.config.DataHandler;

public class ThreadedKiloEssentialsMod extends Thread implements Runnable, KiloThread {
    private Thread thread;
    private String name;
    private Logger logger;

    public ThreadedKiloEssentialsMod(String threadName) {
        name = threadName;
    }

    @Override
    public void start() {
        if (thread == null) {
            thread = new Thread(name);
            thread.start();
        }
        if (logger == null) {
            logger = LogManager.getFormatterLogger(name);
        }
    }


    @Override
    public void run() {
        logger.info("Running thread \"%s@%s\"", thread.getName(), thread.getId());

        new KiloEssentials(
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
