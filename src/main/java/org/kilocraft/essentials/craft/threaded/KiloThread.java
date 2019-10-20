package org.kilocraft.essentials.craft.threaded;

import org.apache.logging.log4j.Logger;

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

    /**
     * The logger of your thread
     * @return a instance of the LogManager
     */
    Logger getLogger();

}
