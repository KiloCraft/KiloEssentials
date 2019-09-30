package org.kilocraft.essentials.api.event.playerEvents;

import net.minecraft.block.Block;
import org.kilocraft.essentials.api.event.Cancellable;
import org.kilocraft.essentials.api.event.Event;
import org.kilocraft.essentials.api.event.Locatable;

public interface PlayerEvent$OnPlaceBlock extends PlayerEvent, Event, Cancellable, Locatable {
    /**
     * Gets the block that has been placed
     * @return the block that has been placed
     */
    Block getBlock();
}
