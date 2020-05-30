package org.kilocraft.essentials.listeners;

import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.player.PlayerStopRidingEvent;
import org.kilocraft.essentials.extensions.betterchairs.SeatManager;

public class OnStopRiding implements EventHandler<PlayerStopRidingEvent> {
    @Override
    public void handle(@NotNull PlayerStopRidingEvent event) {
        if (SeatManager.isEnabled()) {
            SeatManager.getInstance().unseat(KiloServer.getServer().getOnlineUser(event.getPlayer()));
        }
    }
}
