package org.kilocraft.essentials.mixin.patch.performance.entityActivationRange;

import net.minecraft.world.World;
import org.kilocraft.essentials.patch.entityActivationRange.ActivationRange;
import org.kilocraft.essentials.patch.entityActivationRange.WorldInfo;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(World.class)
public abstract class WorldMixin implements WorldInfo {

    public int[] wakeupInactiveRemaining = new int[ActivationRange.ActivationType.values().length];

    @Override
    public int[] getRemaining() {
        return this.wakeupInactiveRemaining;
    }
}
