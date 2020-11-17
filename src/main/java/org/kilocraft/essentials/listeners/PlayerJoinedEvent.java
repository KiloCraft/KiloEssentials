package org.kilocraft.essentials.listeners;

import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.player.PlayerConnectedEvent;
import org.kilocraft.essentials.provided.BrandedServer;
import org.kilocraft.essentials.user.ServerUserManager;

public class PlayerJoinedEvent implements EventHandler<PlayerConnectedEvent> {
    @Override
    public void handle(@NotNull PlayerConnectedEvent event) {
        BrandedServer.provide(event.getPlayer());
        KiloServer.getServer().getMetaManager().onPlayerJoined(event.getPlayer());
        ((ServerUserManager) KiloServer.getServer().getUserManager()).onJoined(event.getPlayer());
    }
}
