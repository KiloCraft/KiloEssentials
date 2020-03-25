package org.kilocraft.essentials.listeners;

import net.minecraft.item.ItemStack;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.player.PlayerInteractBlockEvent;
import org.kilocraft.essentials.extensions.betterchairs.SeatManager;
import org.kilocraft.essentials.util.NbtCommands;

public class OnInteractBlock implements EventHandler<PlayerInteractBlockEvent> {
    @Override
    public void handle(PlayerInteractBlockEvent event) {
        if (event.getPlayer().getStackInHand(event.getHand()) == ItemStack.EMPTY && SeatManager.isEnabled() && event.getHitResult() != null) {
            event.setCancelled(SeatManager.getInstance().onInteractBlock(event.getPlayer(), event.getHitResult(), event.getHand()));
        } else {
            event.setCancelled(NbtCommands.fromRightClick(event.getPlayer(), event.getHand()));
        }
    }
}
