package org.kilocraft.essentials.api;

import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.KiloEssentialsImpl;
import org.kilocraft.essentials.api.server.Server;

public class KiloServer {
    private static Server server;

    /**
     * Get the global server instance
     *
     * @return The server instance
     */
    public static Server getServer() {
        if (server == null)
            throw new RuntimeException("Server isn't set!");

        return server;
    }

    /**
     * Sets the global server instance
     * <b>Should not be used by mods!</b>
     *
     * @param server Server instance
     */
    public static void setServer(Server server) {
        if (KiloServer.server != null)
            throw new RuntimeException("Server is already set!");
        else KiloServer.server = server;

        KiloAPICommands.register(KiloEssentialsImpl.commandDispatcher);
    }

    public static Logger getLogger() {
        return server.getLogger();
    }

}
