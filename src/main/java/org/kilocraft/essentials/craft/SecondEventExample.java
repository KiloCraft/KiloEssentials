package org.kilocraft.essentials.craft;

import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.playerEvents.PlayerEvent$OnBreakingBlockEvent;

public class SecondEventExample implements EventHandler<PlayerEvent$OnBreakingBlockEvent> {
    @Override
    public void handle(PlayerEvent$OnBreakingBlockEvent event) {
        event.getPlayer().sendMessage(new LiteralText("You just broke a block!")
            .setStyle(new Style().setColor(Formatting.GRAY).setColor(Formatting.ITALIC))
        );

        KiloEssentials.getLogger.info(event.getPlayer().getName() + "Just broke a block!");
    }
}
