package org.kilocraft.essentials.events;

import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.serverEvents.ServerEvent$OnAutoSave;
import org.kilocraft.essentials.KiloEssentials;

import java.io.IOException;

public class OnAutoSave implements EventHandler<ServerEvent$OnAutoSave> {
    @Override
    public void handle(ServerEvent$OnAutoSave event) {
        KiloEssentials.getLogger().info("Saving user data...");

        try {
            KiloServer.getServer().getUserManager().triggerSave();
        } catch (IOException e) {
            KiloEssentials.getLogger().error("Can not save the User data!");
            e.printStackTrace();
        }
    }
}
