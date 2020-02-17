package org.kilocraft.essentials.listeners;

import net.minecraft.item.ItemStack;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.player.PlayerInteractBlockEvent;
import org.kilocraft.essentials.extensions.betterchairs.PlayerSitManager;
import org.kilocraft.essentials.util.NbtCommands;

public class OnInteractBlock implements EventHandler<PlayerInteractBlockEvent> {
    @Override
    public void handle(PlayerInteractBlockEvent event) {
        if (event.getHitResult() == null || !PlayerSitManager.enabled)
            return;

        event.setCancelled(event.getPlayer().getStackInHand(event.getHand()) == ItemStack.EMPTY ?
                PlayerSitManager.INSTANCE.onInteractBlock(event.getPlayer(), event.getHitResult(), event.getHand()) :
                NbtCommands.fromRightClick(event.getPlayer(), event.getHand()));
    }
}
