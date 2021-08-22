package org.kilocraft.essentials.mixin.patch.performance.entityActivationRange;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.util.math.BlockPos;
import org.kilocraft.essentials.patch.entityActivationRange.TargetPosition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Goal.class)
public abstract class GoalMixin {

    /**
     * @author Drex
     * @reason reset target position
     */
    @Overwrite
    public void stop() {
        if ((Object) this instanceof MoveToTargetPosGoal) {
            ((TargetPosition) this).setTargetPosition(BlockPos.ORIGIN);
        }
    }


}
