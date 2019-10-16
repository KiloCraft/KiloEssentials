package org.kilocraft.essentials.craft;

import org.kilocraft.essentials.craft.threaded.KiloThread;

public class ThreadManager {
    private static String name;
    private KiloThread kiloThread;
    private Thread thread;
    private ThreadManager threadManager;
    private boolean isMain;

    public <T extends KiloThread & Runnable> ThreadManager(T t) {
        this.kiloThread = t;
        this.thread = new Thread(t);
    }

    public static void setMainName(String string) {
        name = string;
    }

    public void setMainThread(boolean set) {
        isMain = set;
        this.thread.setName(getName());
    }

    public void start() {
        this.thread.setName(getName());
        this.thread.start();
    }

    public void run() {
        this.thread.setName(getName());
        this.thread.run();
    }

    public String getName() {
        return isMain ? name : name + "-" + kiloThread.getName();
    }

    public Thread getThread() {
        return this.thread;
    }

    public KiloThread getKiloThread() {
        return this.kiloThread;
    }
}
