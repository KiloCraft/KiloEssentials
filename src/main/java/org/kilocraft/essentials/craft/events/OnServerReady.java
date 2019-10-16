package org.kilocraft.essentials.craft.events;

import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.serverEvents.ServerEvent$OnReady;
import org.kilocraft.essentials.craft.homesystem.Home;
import org.kilocraft.essentials.craft.homesystem.HomeManager;

import java.util.UUID;

public class OnServerReady implements EventHandler<ServerEvent$OnReady> {
    @Override
    public void handle(ServerEvent$OnReady event) {
        Home home = new Home(
                UUID.randomUUID(),
                "TESTING",
                12,
                13,
                14,
                0,
                10.0F,
                23.0F
        );

        HomeManager.addHome(home);

    }
}
