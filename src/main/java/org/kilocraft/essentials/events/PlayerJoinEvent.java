package org.kilocraft.essentials.events;

import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.playerEvents.PlayerEvent$OnConnect;
import org.kilocraft.essentials.chat.KiloChat;

public class PlayerJoinEvent implements EventHandler<PlayerEvent$OnConnect> {
    @Override
    public void handle(PlayerEvent$OnConnect event) {
        //KiloServer.getServer().getUserManager().onPlayerJoin(event.getPlayer());
        KiloChat.broadcastUserJoinEventMessage(event.getPlayer());
    }
}
