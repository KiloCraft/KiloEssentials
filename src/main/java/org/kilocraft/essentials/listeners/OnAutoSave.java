package org.kilocraft.essentials.listeners;

import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.server.lifecycle.ServerAutosaveEvent;

public class OnAutoSave implements EventHandler<ServerAutosaveEvent> {
    @Override
    public void handle(ServerAutosaveEvent event) {
        KiloServer.getServer().getUserManager().saveAllUsers();
    }
}
