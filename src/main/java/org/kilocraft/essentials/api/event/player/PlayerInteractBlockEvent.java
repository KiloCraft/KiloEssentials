package org.kilocraft.essentials.api.event.player;

import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.kilocraft.essentials.api.event.Cancellable;
import org.kilocraft.essentials.api.event.Event;
import org.kilocraft.essentials.api.event.context.Contextual;
import org.kilocraft.essentials.api.event.context.PlayerContext;

public interface PlayerInteractBlockEvent extends Event, PlayerContext, Contextual, Cancellable {
    BlockHitResult getHitResult();
    Hand getHand();
}
