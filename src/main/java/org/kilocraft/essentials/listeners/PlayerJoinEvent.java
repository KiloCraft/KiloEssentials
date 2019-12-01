package org.kilocraft.essentials.listeners;

import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.player.PlayerConnectEvent;
import org.kilocraft.essentials.user.ServerUserManager;

public class PlayerJoinEvent implements EventHandler<PlayerConnectEvent> {
    @Override
    public void handle(PlayerConnectEvent event) {
        ((ServerUserManager) KiloServer.getServer().getUserManager()).onJoin(event.getPlayer());
    }
}
