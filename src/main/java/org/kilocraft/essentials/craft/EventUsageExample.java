package org.kilocraft.essentials.craft;

import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.playerEvents.OnPlayerConnectEvent;

public class EventUsageExample implements EventHandler<OnPlayerConnectEvent> {

    @Override
    public void handle(OnPlayerConnectEvent event) {
        event.getPlayer().getServer().getPlayerManager().sendToAll(new LiteralText(event.getPlayer().getName() + "joined.").setStyle(new Style().setColor(Formatting.GRAY)));

    }
}
