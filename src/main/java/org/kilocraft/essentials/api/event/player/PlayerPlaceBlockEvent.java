package org.kilocraft.essentials.api.event.player;

import net.minecraft.block.Block;
import org.kilocraft.essentials.api.event.Cancellable;
import org.kilocraft.essentials.api.event.Event;
import org.kilocraft.essentials.api.event.context.LocationContext;
import org.kilocraft.essentials.api.event.context.PlayerContext;
import org.kilocraft.essentials.api.event.context.WorldContext;

public interface PlayerPlaceBlockEvent extends Event, PlayerContext, LocationContext, WorldContext, Cancellable {
    /**
     * Gets the block that has been placed
     * @return the block that has been placed
     */
    Block getBlock();
}
