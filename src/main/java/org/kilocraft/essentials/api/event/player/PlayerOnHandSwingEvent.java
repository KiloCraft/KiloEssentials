package org.kilocraft.essentials.api.event.player;

import net.minecraft.util.Hand;
import org.kilocraft.essentials.api.event.Cancellable;
import org.kilocraft.essentials.api.event.Event;
import org.kilocraft.essentials.api.event.context.Contextual;
import org.kilocraft.essentials.api.event.context.PlayerContext;

public interface PlayerOnHandSwingEvent extends Event, PlayerContext, Contextual, Cancellable {
    Hand getHand();
}
