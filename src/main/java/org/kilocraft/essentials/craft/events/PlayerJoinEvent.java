package org.kilocraft.essentials.craft.events;

import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.playerEvents.PlayerEvent$OnConnect;

public class PlayerJoinEvent implements EventHandler<PlayerEvent$OnConnect> {
    @Override
    public void handle(PlayerEvent$OnConnect event) {
        LiteralText literalText = new LiteralText(event.getPlayer().getName() + " joined.");
        event.getServer().getPlayerManager().sendToAll(literalText.formatted(Formatting.GRAY));
    }
}
