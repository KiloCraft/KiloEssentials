package org.kilocraft.essentials.listeners;

import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.server.lifecycle.ServerReadyEvent;
import org.kilocraft.essentials.provided.BrandedServer;

public class OnServerReady implements EventHandler<ServerReadyEvent> {
    @Override
    public void handle(ServerReadyEvent event) {
        BrandedServer.set();
    }
}
