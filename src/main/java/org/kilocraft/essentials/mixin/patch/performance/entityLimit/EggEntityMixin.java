package org.kilocraft.essentials.mixin.patch.performance.entityLimit;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.world.World;
import org.kilocraft.essentials.api.util.TickManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(EggEntity.class)
public abstract class EggEntityMixin extends ThrownItemEntity {

    public EggEntityMixin(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    @Redirect(method = "onCollision", at = @At(value = "INVOKE", target = "Ljava/util/Random;nextInt(I)I", ordinal = 0))
    public int shouldSpawn(Random random, int bound) {
        return TickManager.isEntityLimitReached(this.world, getBlockPos(), EntityType.CHICKEN) ? 1 : random.nextInt(8);
    }

}
