package org.kilocraft.essentials.craft.events;

import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.serverEvents.ServerEvent$OnReady;
import org.kilocraft.essentials.craft.provider.KiloBrandName;

public class OnServerReady implements EventHandler<ServerEvent$OnReady> {
    @Override
    public void handle(ServerEvent$OnReady event) {
        KiloBrandName.provide();
    }
}
