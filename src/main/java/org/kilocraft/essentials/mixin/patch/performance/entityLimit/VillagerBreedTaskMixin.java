package org.kilocraft.essentials.mixin.patch.performance.entityLimit;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.brain.task.VillagerBreedTask;
import net.minecraft.entity.passive.VillagerEntity;
import org.kilocraft.essentials.api.util.TickManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(VillagerBreedTask.class)
public abstract class VillagerBreedTaskMixin {

    // Abort villager breed logic if entity limit is reached
    @Redirect(method = "keepRunning", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/VillagerEntity;squaredDistanceTo(Lnet/minecraft/entity/Entity;)D"))
    public double noIntenseBreeding(VillagerEntity villagerEntity, Entity entity) {
        return TickManager.isEntityLimitReached(villagerEntity.getEntityWorld(), villagerEntity.getBlockPos(), EntityType.VILLAGER) ? Integer.MAX_VALUE : villagerEntity.squaredDistanceTo(entity);
    }

}
