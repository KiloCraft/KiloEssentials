package org.kilocraft.essentials.mixin.patch.performance.entityLimit;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import org.kilocraft.essentials.api.util.TickManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(GuardianEntity.class)
public abstract class GuardianEntityMixin {

    // Abort guardian spawn logic if entity limit is reached
    @Inject(method = "canSpawn(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/WorldAccess;Lnet/minecraft/entity/SpawnReason;Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;)Z", at = @At(value = "HEAD"), cancellable = true)
    private static void shouldSpawn(EntityType<? extends GuardianEntity> entityType, WorldAccess world, SpawnReason spawnReason, BlockPos blockPos, Random random, CallbackInfoReturnable<Boolean> cir) {
        if (TickManager.isEntityLimitReached(world, blockPos, EntityType.GUARDIAN)) cir.setReturnValue(false);
    }
}
