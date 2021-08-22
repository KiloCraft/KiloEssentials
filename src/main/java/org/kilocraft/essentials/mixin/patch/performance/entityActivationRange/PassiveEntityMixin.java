package org.kilocraft.essentials.mixin.patch.performance.entityActivationRange;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.world.World;
import org.kilocraft.essentials.patch.entityActivationRange.InactiveEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PassiveEntity.class)
public abstract class PassiveEntityMixin extends PathAwareEntity implements InactiveEntity {

    @Shadow public abstract int getBreedingAge();

    @Shadow public abstract void setBreedingAge(int i);

    protected PassiveEntityMixin(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public void inactiveTick() {
        ++this.despawnCounter;
        if (this.world.isClient) {
            this.calculateDimensions();
        } else {
            int i = this.getBreedingAge();
            if (i < 0) {
                i++;
                this.setBreedingAge(i);
            } else if (i > 0) {
                i--;
                this.setBreedingAge(i);
            }
        }
    }
}
