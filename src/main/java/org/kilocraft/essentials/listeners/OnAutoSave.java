package org.kilocraft.essentials.listeners;

import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.KiloEssentialsImpl;
import org.kilocraft.essentials.api.event.server.lifecycle.ServerAutosaveEvent;

import java.io.IOException;

public class OnAutoSave implements EventHandler<ServerAutosaveEvent> {
    @Override
    public void handle(ServerAutosaveEvent event) {
        KiloEssentialsImpl.getLogger().info("Saving user data...");

        try {
            KiloServer.getServer().getUserManager().triggerSave();
        } catch (IOException e) {
            KiloEssentialsImpl.getLogger().error("Can not save the User data!");
            e.printStackTrace();
        }
    }
}