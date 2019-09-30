package org.kilocraft.essentials.api.event.playerEvents;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import org.kilocraft.essentials.api.event.Cancellable;
import org.kilocraft.essentials.api.event.Locatable;

public interface PlayerEvent$OnBreakingBlockEvent extends PlayerEvent, Cancellable, Locatable {

    /**
     * Gets the block that has been broken
     * @return the block that has been broken
     */
    BlockState getBlockState();

    Block getBlock();

}
