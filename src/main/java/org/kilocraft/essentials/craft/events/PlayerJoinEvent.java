package org.kilocraft.essentials.craft.events;

import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.playerEvents.PlayerEvent$OnConnect;
import org.kilocraft.essentials.craft.chat.ChatMessage;
import org.kilocraft.essentials.craft.chat.KiloChat;
import org.kilocraft.essentials.craft.config.KiloConifg;
import org.kilocraft.essentials.craft.config.provided.localvariables.PlayerConfigVariables;

public class PlayerJoinEvent implements EventHandler<PlayerEvent$OnConnect> {
    @Override
    public void handle(PlayerEvent$OnConnect event) {

        KiloServer.getServer().getUserManager().onPlayerJoin(event.getPlayer());

        KiloChat.broadCast(new ChatMessage(
                KiloConifg.getProvider().getMessages().getLocal(
                        true,
                        "general.joinMessage",
                        new PlayerConfigVariables(event.getPlayer())
                ),
                true
        ));

    }
}
