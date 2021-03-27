package org.kilocraft.essentials.mixin.entityActivationRange;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.world.World;
import org.kilocraft.essentials.patch.entityActivationRange.InactiveEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ArrowEntity.class)
public abstract class ArrowEntityEntity extends PersistentProjectileEntity implements InactiveEntity {

    protected ArrowEntityEntity(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public void inactiveTick() {
        if (this.inGround) {
            this.inGroundTime++;
        }
    }

}
