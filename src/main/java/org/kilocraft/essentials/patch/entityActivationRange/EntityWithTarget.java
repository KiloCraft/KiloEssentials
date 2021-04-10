package org.kilocraft.essentials.patch.entityActivationRange;

import net.minecraft.util.math.BlockPos;

public interface EntityWithTarget {

    public BlockPos getMovingTarget();

    public void setMovingTarget(BlockPos pos);

}
