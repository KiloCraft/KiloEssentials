package org.kilocraft.essentials.craft.events;

import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.playerEvents.PlayerEvent$OnDisconnect;

public class PlayerLeaveEvent implements EventHandler<PlayerEvent$OnDisconnect> {
    @Override
    public void handle(PlayerEvent$OnDisconnect event) {
        LiteralText text = (LiteralText) new LiteralText(String.format("%s Left.", event.getPlayer().getName())).formatted(Formatting.GRAY);
        event.getServer().getPlayerManager().broadcastChatMessage(text, false);    }
}
