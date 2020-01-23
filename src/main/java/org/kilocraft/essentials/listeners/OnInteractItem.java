package org.kilocraft.essentials.listeners;

import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.player.PlayerInteractItem;
import org.kilocraft.essentials.util.NbtCommands;

public class OnInteractItem implements EventHandler<PlayerInteractItem> {
    @Override
    public void handle(PlayerInteractItem event) {
        event.setReturnValue(NbtCommands.trigger(event.getPlayer(), event.getWorld(), event.getItemStack(), event.getHand()));
    }
}
