package org.kilocraft.essentials.events.server;

import net.minecraft.server.MinecraftServer;
import org.kilocraft.essentials.api.event.server.lifecycle.ServerSaveEvent;

public class ServerSaveEventImpl implements ServerSaveEvent {
    private MinecraftServer server;

    public ServerSaveEventImpl(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public MinecraftServer getServer() {
        return server;
    }
}
