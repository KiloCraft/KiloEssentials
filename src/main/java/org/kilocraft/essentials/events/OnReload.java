package org.kilocraft.essentials.events;

import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.serverEvents.ServerEvent$OnReload;
import org.kilocraft.essentials.worldwarps.WarpManager;

public class OnReload implements EventHandler<ServerEvent$OnReload> {
    @Override
    public void handle(ServerEvent$OnReload event) {
        WarpManager.INSTANCE.reload();
    }
}
