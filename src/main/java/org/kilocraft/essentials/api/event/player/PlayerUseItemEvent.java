package org.kilocraft.essentials.api.event.player;

import net.minecraft.util.Hand;
import org.kilocraft.essentials.api.event.Cancellable;
import org.kilocraft.essentials.api.event.Event;
import org.kilocraft.essentials.api.event.context.PlayerContext;
import org.kilocraft.essentials.api.event.context.WorldContext;

public interface PlayerUseItemEvent extends Event, PlayerContext, WorldContext, Cancellable {

    Hand getHand();
}
