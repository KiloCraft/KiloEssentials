package org.kilocraft.essentials.api.event.player;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.kilocraft.essentials.api.event.Cancellable;
import org.kilocraft.essentials.api.event.Event;
import org.kilocraft.essentials.api.event.context.PlayerContext;
import org.kilocraft.essentials.api.event.context.ReturnableContext;
import org.kilocraft.essentials.api.event.context.WorldContext;

public interface PlayerInteractItem extends Event, PlayerContext, WorldContext, Cancellable, ReturnableContext<ActionResult> {
    ItemStack getItemStack();
    Hand getHand();
}
