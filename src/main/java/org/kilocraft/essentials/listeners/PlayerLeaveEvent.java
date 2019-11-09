package org.kilocraft.essentials.listeners;

import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.player.PlayerDisconnectEvent;
import org.kilocraft.essentials.chat.KiloChat;

public class PlayerLeaveEvent implements EventHandler<PlayerDisconnectEvent> {
    @Override
    public void handle(PlayerDisconnectEvent event) {
        //KiloServer.getServer().getUserManager().onPlayerLeave(event.getPlayer());
        KiloChat.broadcastUserLeaveEventMessage(event.getPlayer());
    }
}
