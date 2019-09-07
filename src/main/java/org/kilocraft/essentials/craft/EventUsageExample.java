package org.kilocraft.essentials.craft;

import net.minecraft.text.LiteralText;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.playerEvents.PlayerEvent$OnPlaceBlock;

public class EventUsageExample implements EventHandler<PlayerEvent$OnPlaceBlock> {
    @Override
    public void handle(PlayerEvent$OnPlaceBlock event) {
        event.getPlayer().sendMessage(new LiteralText("You placed a new " + event.getBlock() + "block!"));
    }
}
