package org.kilocraft.essentials.mixin.patch;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.math.Box;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.kilocraft.essentials.util.settings.ServerSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(EggEntity.class)
public abstract class EggEntityMixin extends ThrownItemEntity {

    public EggEntityMixin(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    @Redirect(method = "onCollision", at = @At(value = "INVOKE", target = "Ljava/util/Random;nextInt(I)I",ordinal = 0))
    public int shouldSpawn(Random random, int bound) {
        int i = random.nextInt(8);
        int range = ServerSettings.getInt("entity_limit." + Registry.ENTITY_TYPE.getId(EntityType.CHICKEN).getPath() + ".range");
        int limit = ServerSettings.getInt("entity_limit." + Registry.ENTITY_TYPE.getId(EntityType.CHICKEN).getPath() + ".limit");
        if (range !=-1 && limit !=-1) {
            if (limit <= this.world.getEntitiesByType(EntityType.CHICKEN, new Box(getBlockPos().mutableCopy().add(range, range, range), getBlockPos().mutableCopy().add(-range, -range, -range)), EntityPredicates.EXCEPT_SPECTATOR).size()) {
                return 1;
            }
        }
        return i;
    }

}
