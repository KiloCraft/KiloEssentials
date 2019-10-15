package org.kilocraft.essentials.craft.events;

import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.serverEvents.ServerEvent$OnReload;
import org.kilocraft.essentials.craft.homesystem.HomeManager;
import org.kilocraft.essentials.craft.worldwarps.WarpManager;

public class OnReload implements EventHandler<ServerEvent$OnReload> {
    @Override
    public void handle(ServerEvent$OnReload event) {
        WarpManager.INSTANCE.reload();
        HomeManager.INSTANCE.reload();
    }
}
