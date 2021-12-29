package org.kilocraft.essentials.mixin.patch.performance.entityLimit;

import org.kilocraft.essentials.api.util.TickManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.level.LevelAccessor;

@Mixin(Guardian.class)
public abstract class GuardianMixin {

    // Abort guardian spawn logic if entity limit is reached
    @Inject(
            method = "checkGuardianSpawnRules",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void shouldSpawn(EntityType<? extends Guardian> entityType, LevelAccessor world, MobSpawnType spawnReason, BlockPos blockPos, Random random, CallbackInfoReturnable<Boolean> cir) {
        if (TickManager.isEntityLimitReached(world, blockPos, EntityType.GUARDIAN)) cir.setReturnValue(false);
    }
}
