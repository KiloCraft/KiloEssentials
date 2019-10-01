package org.kilocraft.essentials.craft;

import org.kilocraft.essentials.craft.threaded.KiloThread;

public class ThreadManager {
    private String name;
    private KiloThread kiloThread;
    private Thread thread;

    public ThreadManager() {
    }

    public void setMainName(String string) {
        name = string;
    }

    public <T extends KiloThread & Runnable> void register(T t) {
        this.kiloThread = t;
        this.thread = new Thread(t);
        this.thread.setName(getName());
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

    public KiloThread getKiloThread() {
        return this.kiloThread;
    }
}
