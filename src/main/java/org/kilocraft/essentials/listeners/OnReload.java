package org.kilocraft.essentials.listeners;

import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.server.lifecycle.ServerReloadEvent;
import org.kilocraft.essentials.extensions.warps.WarpManager;

public class OnReload implements EventHandler<ServerReloadEvent> {
    @Override
    public void handle(ServerReloadEvent event) {
        WarpManager.INSTANCE.reload();
    }
}
