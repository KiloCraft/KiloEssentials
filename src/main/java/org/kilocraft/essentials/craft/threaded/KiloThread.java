package org.kilocraft.essentials.craft.threaded;

import org.apache.logging.log4j.Logger;

public interface KiloThread {
    /**
     * The method to start the thread
     */
    void start();

    /**
     * The method that runs everything inside of it when called
     */
    void run();

    /**
     * Gets the FormatterLogManager for the thread
     * @return a instance of the log manager with the same name as the thread
     */
    Logger getLogger();
}
