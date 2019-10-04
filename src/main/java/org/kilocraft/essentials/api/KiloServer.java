package org.kilocraft.essentials.api;

import org.kilocraft.essentials.api.commands.KiloAPICommands;
import org.kilocraft.essentials.api.server.Server;
import org.kilocraft.essentials.api.util.SomeGlobals;

import java.io.File;

public class KiloServer {
    private static Server server;
    private String dataDir = System.getProperty("user.dir") + "/KiloEssentials/data/";
    private File PLAYER_CACHE_FILE = new File(dataDir + "cache.json");
    private File PLAYER_DATA_FILE = new File(dataDir + "players.dat");

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

        KiloAPICommands.register(SomeGlobals.commandDispatcher);
    }


}
