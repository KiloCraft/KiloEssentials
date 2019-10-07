package org.kilocraft.essentials.craft;

import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.craft.threaded.KiloThread;

public class ThreadManager {
    private static String name;
    private KiloThread kiloThread;
    private Thread thread;
    private ThreadManager threadManager;

    public <T extends KiloThread & Runnable> ThreadManager(T t) {
        this.kiloThread = t;
        this.thread = new Thread(t);
        this.thread.setName(getName());
    }

    public static void setMainName(String string) {
        name = string;
    }

    public void start() {
        this.thread.start();
    }

    public void run() {
        this.thread.run();
    }

    public String getName() {
        return name + "-" + this.kiloThread.getName();
    }

    public Thread getThread() {
        return this.thread;
    }

    public Logger getLogger() {
        return this.kiloThread.getLogger();
    }

    public KiloThread getKiloThread() {
        return this.kiloThread;
    }
}
