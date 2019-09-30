package org.kilocraft.essentials.craft.threaded;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.craft.newconfig.NewKiloConifg;

public class ThreadedKiloConfig extends Thread implements Runnable, KiloThread {
    private Thread thread;
    private String name;
    private Logger logger;
    private NewKiloConifg newKiloConifg;

    public ThreadedKiloConfig(String threadName) {
        name = threadName;

        if (thread == null) {
            thread = new Thread(name);
        }
        if (logger == null) {
            logger = LogManager.getFormatterLogger(name);
        }
    }

    public void start() {
        if (thread == null) {
            thread = new Thread(name);
        }
        if (logger == null) {
            logger = LogManager.getFormatterLogger(name);
        }
    }

    public void run() {
        logger.info("Running thread \"%s@%s\"", thread.getName(), thread.getId());

        newKiloConifg = new NewKiloConifg();
    }

    public NewKiloConifg getKiloConfig() {
        return newKiloConifg;
    }

    @Override
    public Logger getLogger() {
        return null;
    }
}
