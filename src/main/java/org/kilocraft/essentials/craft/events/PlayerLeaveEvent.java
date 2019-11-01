package org.kilocraft.essentials.craft.events;

import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.playerEvents.PlayerEvent$OnDisconnect;
import org.kilocraft.essentials.craft.chat.ChatMessage;
import org.kilocraft.essentials.craft.chat.KiloChat;
import org.kilocraft.essentials.craft.commands.essentials.MessageCommand;
import org.kilocraft.essentials.craft.config.KiloConifg;
import org.kilocraft.essentials.craft.config.provided.localvariables.PlayerConfigVariables;

public class PlayerLeaveEvent implements EventHandler<PlayerEvent$OnDisconnect> {
    @Override
    public void handle(PlayerEvent$OnDisconnect event) {

        KiloServer.getServer().getUserManager().onPlayerLeave(event.getPlayer());

        KiloChat.broadCast(new ChatMessage(
                KiloConifg.getProvider().getMessages().getLocal(
                        true,
                        "general.leaveMessage",
                        new PlayerConfigVariables(event.getPlayer())
                ),
                true
        ));

        MessageCommand.stringSaverProvider.remove(event.getPlayer().getCommandSource().getName());
    }
}
