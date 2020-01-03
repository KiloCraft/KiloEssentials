package org.kilocraft.essentials.threaded;

import org.apache.logging.log4j.Logger;

public interface KiloThread {
    /**
     * Name of the thread
     * @return ""
     */
    String getName();

    /**
     * The logger of your thread
     * @return a instance of the LogManager
     */
    Logger getLogger();
}
