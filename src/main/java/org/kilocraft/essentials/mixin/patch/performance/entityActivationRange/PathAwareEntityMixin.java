package org.kilocraft.essentials.mixin.patch.performance.entityActivationRange;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import org.kilocraft.essentials.patch.entityActivationRange.EntityWithTarget;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PathAwareEntity.class)
public abstract class PathAwareEntityMixin implements EntityWithTarget {

    public BlockPos movingTarget = null;

    @Override
    public BlockPos getMovingTarget() {
        return movingTarget;
    }

    @Override
    public void setMovingTarget(BlockPos pos) {
        this.movingTarget = pos;
    }

}
