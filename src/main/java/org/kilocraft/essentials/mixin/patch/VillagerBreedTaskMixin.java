package org.kilocraft.essentials.mixin.patch;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.brain.task.VillagerBreedTask;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.math.Box;
import org.kilocraft.essentials.util.settings.ServerSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(VillagerBreedTask.class)
public abstract class VillagerBreedTaskMixin {

    @Redirect(method = "keepRunning", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/VillagerEntity;squaredDistanceTo(Lnet/minecraft/entity/Entity;)D"))
    public double noIntenseBreeding(VillagerEntity villagerEntity, Entity entity) {
        int range = ServerSettings.getInt("entity_limit.villager.range");
        int limit = ServerSettings.getInt("entity_limit.villager.limit");
        if (range != -1 && limit != -1) {
            if (limit <= villagerEntity.getEntityWorld().getEntitiesByType(villagerEntity.getType(), new Box(villagerEntity.getBlockPos().mutableCopy().add(range, range, range), villagerEntity.getBlockPos().mutableCopy().add(-range, -range, -range)), EntityPredicates.EXCEPT_SPECTATOR).size()) {
                return 6;
            }
        }
        return villagerEntity.squaredDistanceTo(entity);
    }

}
