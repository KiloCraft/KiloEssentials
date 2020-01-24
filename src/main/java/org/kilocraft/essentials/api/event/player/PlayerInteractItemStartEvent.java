package org.kilocraft.essentials.api.event.player;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.kilocraft.essentials.api.event.Cancellable;
import org.kilocraft.essentials.api.event.Event;
import org.kilocraft.essentials.api.event.context.Contextual;
import org.kilocraft.essentials.api.event.context.PlayerContext;
import org.kilocraft.essentials.api.event.context.WorldContext;

public interface PlayerInteractItemStartEvent extends Event, PlayerContext, WorldContext, Cancellable, Contextual {
    ItemStack getItemStack();
    Hand getHand();
}
