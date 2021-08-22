package org.kilocraft.essentials.mixin.patch.performance.entityActivationRange;

import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import org.kilocraft.essentials.patch.entityActivationRange.GoalSelectorInterface;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;

@Mixin(GoalSelector.class)
public abstract class GoalSelectorMixin implements GoalSelectorInterface {

    @Shadow private int timeInterval;
    @Shadow @Final private Set<PrioritizedGoal> goals;
    private int curRate;

    private int getCurRate() {
        return curRate;
    }

    private void incRate() {
        this.curRate++;
    }

    @Override
    public boolean inactiveTick() {
        incRate();
        return getCurRate() % timeInterval == 0;
    }

    @Override
    public boolean hasTasks() {
        for (PrioritizedGoal task : goals) {
            if (task.isRunning()) {
                return true;
            }
        }
        return false;
    }


}
