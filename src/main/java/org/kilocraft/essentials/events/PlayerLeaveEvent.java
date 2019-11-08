package org.kilocraft.essentials.events;

import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.playerEvents.PlayerEvent$OnDisconnect;
import org.kilocraft.essentials.chat.KiloChat;

public class PlayerLeaveEvent implements EventHandler<PlayerEvent$OnDisconnect> {
    @Override
    public void handle(PlayerEvent$OnDisconnect event) {
        //KiloServer.getServer().getUserManager().onPlayerLeave(event.getPlayer());
        KiloChat.broadcastUserLeaveEventMessage(event.getPlayer());
    }
}
