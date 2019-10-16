package org.kilocraft.essentials.craft.threaded;

import org.kilocraft.essentials.craft.config.KiloConifg;

public class ThreadedKiloConfig implements Runnable, KiloThread {
    @Override
    public String getName() {
        return "Config";
    }

    @Override
    public void run() {
        new KiloConifg();
        KiloConifg.load();
    }

}
