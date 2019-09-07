package org.kilocraft.essentials.craft.events;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.playerEvents.PlayerEvent$OnConnect;

public class PlayerJoinEvent implements EventHandler<PlayerEvent$OnConnect> {
    @Override
    public void handle(PlayerEvent$OnConnect event) {
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
        player.getServer().getPlayerManager().sendToAll(new LiteralText( player.getName() + " joined."));

    }
}
