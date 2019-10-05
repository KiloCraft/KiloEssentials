package org.kilocraft.essentials.craft.threaded;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.craft.config.KiloConifg;

public class ThreadedKiloConfig implements Runnable, KiloThread {
    private Logger logger;
    private KiloConifg KiloConifg;

    public ThreadedKiloConfig() {
    }

    @Override
    public String getName() {
        return "Config";
    }

    public void run() {
        this.logger = LogManager.getFormatterLogger();
        KiloConifg = new KiloConifg();
    }

    public KiloConifg getKiloConfig() {
        return KiloConifg;
    }

    @Override
    public Logger getLogger() {
        return this.logger;
    }
}
