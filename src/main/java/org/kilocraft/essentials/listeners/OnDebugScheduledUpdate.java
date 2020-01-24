package org.kilocraft.essentials.listeners;

import org.kilocraft.essentials.KiloEssentialsImpl;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.server.lifecycle.ServerDebugScheduledUpdateEvent;

public class OnDebugScheduledUpdate implements EventHandler<ServerDebugScheduledUpdateEvent> {
    @Override
    public void handle(ServerDebugScheduledUpdateEvent event) {
        KiloEssentialsImpl.getInstance().getDebugUtils().onScheduledUpdate();
    }
}
