package org.kilocraft.essentials.api.event.playerEvents;

import org.kilocraft.essentials.api.event.Cancellable;
import org.kilocraft.essentials.api.event.Event;
import org.kilocraft.essentials.api.event.Locatable;
import org.kilocraft.essentials.api.world.Block;

public interface OnPlayerPlaceBlockEvent extends Event, Cancellable, Locatable {
    /**
     * Gets the block that has been placed
     * @return the block that has been placed
     */
    Block getBlock();
}
