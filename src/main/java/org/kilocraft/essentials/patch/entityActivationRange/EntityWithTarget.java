package org.kilocraft.essentials.patch.entityActivationRange;

import net.minecraft.util.math.BlockPos;

public interface EntityWithTarget {

    BlockPos getMovingTarget();

    void setMovingTarget(BlockPos pos);

}
