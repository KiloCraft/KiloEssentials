package org.kilocraft.essentials.listeners;

import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.player.PlayerInteractBlockEvent;
import org.kilocraft.essentials.extensions.betterchairs.PlayerSitManager;

public class OnInteractBlock implements EventHandler<PlayerInteractBlockEvent> {
    @Override
    public void handle(PlayerInteractBlockEvent event) {
        if (event.getHitResult() == null)
            return;

        event.setCancelled(PlayerSitManager.INSTANCE.onInteractBlock(event.getPlayer(), event.getHitResult(), event.getHand()));
    }
}
