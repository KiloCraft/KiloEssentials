package org.kilocraft.essentials.listeners;

import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.player.PlayerDisconnectEvent;
import org.kilocraft.essentials.chat.ServerChat;
import org.kilocraft.essentials.user.ServerUserManager;

public class PlayerLeaveEvent implements EventHandler<PlayerDisconnectEvent> {
    @Override
    public void handle(PlayerDisconnectEvent event) {
        ((ServerUserManager) KiloServer.getServer().getUserManager()).onLeave(event.getPlayer());
        ServerChat.removeSocialSpy(event.getPlayer());
    }
}
