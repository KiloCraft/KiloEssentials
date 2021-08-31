package org.kilocraft.essentials.mixin.patch.performance.entityActivationRange;

import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;
import org.kilocraft.essentials.patch.entityActivationRange.InactiveEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AreaEffectCloudEntity.class)
public abstract class AreaEffectCloudEntityMixin extends Entity implements InactiveEntity {

    @Shadow
    private int waitTime;

    @Shadow
    private int duration;

    public AreaEffectCloudEntityMixin(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public void inactiveTick() {
        if (this.age >= this.waitTime + this.duration) {
            this.discard();
        }
    }
}
