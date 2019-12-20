package org.kilocraft.essentials.api.event.player;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import org.kilocraft.essentials.api.event.Cancellable;
import org.kilocraft.essentials.api.event.Event;
import org.kilocraft.essentials.api.event.context.LocationContext;
import org.kilocraft.essentials.api.event.context.PlayerContext;
import org.kilocraft.essentials.api.event.context.WorldContext;

public interface PlayerBreakBlockEvent extends Event, PlayerContext, LocationContext, WorldContext, Cancellable {
    /**
     * Gets the block that has been broken
     * @return the block that has been broken
     */
    BlockState getBlockState();

    Block getBlock();

}
