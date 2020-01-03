package org.kilocraft.essentials.listeners;

import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.server.lifecycle.ServerScheduledUpdateEvent;

public class OnScheduledUpdate implements EventHandler<ServerScheduledUpdateEvent> {
    @Override
    public void handle(ServerScheduledUpdateEvent event) {
        KiloServer.getServer().getMetaManager().updateAll();

    }
}
