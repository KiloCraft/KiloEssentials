package org.kilocraft.essentials.craft.events;

import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.playerEvents.PlayerEvent$OnConnect;

public class PlayerJoinEvent implements EventHandler<PlayerEvent$OnConnect> {
    @Override
    public void handle(PlayerEvent$OnConnect event) {
        LiteralText text = (LiteralText) new LiteralText(String.format("%s Joined.", event.getPlayer().getName())).formatted(Formatting.GRAY);
        event.getServer().getPlayerManager().broadcastChatMessage(text, false);
    }
}
