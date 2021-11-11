package org.kilocraft.essentials.mixin.patch.bugfix;

import net.minecraft.entity.EntityData;
import net.minecraft.entity.passive.PassiveEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PassiveEntity.class)
public abstract class PassiveEntityMixin {

    /**
     * Fixes a ClassCastException caused by passive entities casting ZombieEntity$ZombieData to PassiveEntity$PassiveData
     */
    @ModifyVariable(
            method = "initialize",
            at = @At("HEAD"),
            index = 4
    )
    private EntityData init(EntityData entityData) {
        if (!(entityData instanceof PassiveEntity.PassiveData)) {
            return new PassiveEntity.PassiveData(true);
        }
        return entityData;
    }

}
