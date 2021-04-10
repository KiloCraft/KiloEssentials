package org.kilocraft.essentials.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.WorldAccess;
import org.kilocraft.essentials.util.settings.ServerSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(GuardianEntity.class)
public class GuardianEntityMixin {

    @Inject(method = "canSpawn(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/WorldAccess;Lnet/minecraft/entity/SpawnReason;Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;)Z", at = @At(value = "HEAD"), cancellable = true)
    private static void shouldSpawn(EntityType<? extends GuardianEntity> entityType, WorldAccess worldAccess, SpawnReason spawnReason, BlockPos blockPos, Random random, CallbackInfoReturnable<Boolean> cir) {
        int range = ServerSettings.getInt("entity_limit." + Registry.ENTITY_TYPE.getId(EntityType.GUARDIAN).getPath() + ".range");
        int limit = ServerSettings.getInt("entity_limit." + Registry.ENTITY_TYPE.getId(EntityType.GUARDIAN).getPath() + ".limit");
        if (range !=-1 && limit !=-1) {
            if (limit <= worldAccess.getEntitiesByType(entityType, new Box(blockPos.mutableCopy().add(range, range, range), blockPos.mutableCopy().add(-range, -range, -range)), EntityPredicates.EXCEPT_SPECTATOR).size()) {
                cir.setReturnValue(false);
            }
        }
    }
}
