package org.kilocraft.essentials.listeners;

import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.player.PlayerStopRidingEvent;
import org.kilocraft.essentials.extensions.betterchairs.PlayerSitManager;

public class OnStopRiding implements EventHandler<PlayerStopRidingEvent> {
    @Override
    public void handle(PlayerStopRidingEvent event) {
        PlayerSitManager.INSTANCE.onStopRiding(event.getPlayer());
    }
}
