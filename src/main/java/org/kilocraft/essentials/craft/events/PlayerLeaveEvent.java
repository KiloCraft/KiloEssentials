package org.kilocraft.essentials.craft.events;

import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.playerEvents.PlayerEvent$OnDisconnect;

public class PlayerLeaveEvent implements EventHandler<PlayerEvent$OnDisconnect> {
    @Override
    public void handle(PlayerEvent$OnDisconnect event) {
        event.getServer().getPlayerManager().sendToAll(new LiteralText(event.getPlayer().getName().asFormattedString() + "left.").formatted(Formatting.GRAY));
    }
}
