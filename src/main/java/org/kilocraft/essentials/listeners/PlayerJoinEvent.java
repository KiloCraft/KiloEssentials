package org.kilocraft.essentials.listeners;

import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.player.PlayerConnectEvent;

public class PlayerJoinEvent implements EventHandler<PlayerConnectEvent> {
    @Override
    public void handle(PlayerConnectEvent event) {
        KiloServer.getServer().getUserManager().onPlayerJoin(event.getPlayer());
        //KiloChat.broadcastUserJoinEventMessage(event.getPlayer());
    }
}
