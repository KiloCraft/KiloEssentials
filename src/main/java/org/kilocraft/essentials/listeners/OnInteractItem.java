package org.kilocraft.essentials.listeners;

import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.player.PlayerInteractItemStartEvent;
import org.kilocraft.essentials.util.NbtCommands;

public class OnInteractItem implements EventHandler<PlayerInteractItemStartEvent> {
    @Override
    public void handle(PlayerInteractItemStartEvent event) {
        event.setCancelled(NbtCommands.fromRightClick(event.getPlayer(), event.getHand()));
    }
}
