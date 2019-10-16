package org.kilocraft.essentials.craft.threaded;

public interface KiloThread {

    /**
     * Name of the thread
     * @return ""
     */
    String getName();

    /**
     * The method that runs everything inside of it when called
     */
    void run();

}
