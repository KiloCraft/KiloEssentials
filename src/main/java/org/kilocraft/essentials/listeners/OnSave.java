package org.kilocraft.essentials.listeners;

import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.server.lifecycle.ServerSaveEvent;

public class OnSave implements EventHandler<ServerSaveEvent> {
    @Override
    public void handle(ServerSaveEvent event) {
        KiloServer.getServer().getUserManager().saveAllUsers();

    }
}
