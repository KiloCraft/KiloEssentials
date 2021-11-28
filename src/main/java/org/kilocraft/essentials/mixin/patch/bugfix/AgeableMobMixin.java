package org.kilocraft.essentials.mixin.patch.bugfix;

import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.SpawnGroupData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(AgeableMob.class)
public abstract class AgeableMobMixin {

    /**
     * Fixes a ClassCastException caused by passive entities casting ZombieEntity$ZombieData to PassiveEntity$PassiveData
     */
    @ModifyVariable(
            method = "finalizeSpawn",
            at = @At("HEAD"),
            index = 4
    )
    private SpawnGroupData init(SpawnGroupData entityData) {
        if (!(entityData instanceof AgeableMob.AgeableMobGroupData)) {
            return new AgeableMob.AgeableMobGroupData(true);
        }
        return entityData;
    }

}
