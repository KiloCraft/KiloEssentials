package org.kilocraft.essentials.mixin.patch.performance.optimizedSpawning;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GravityField;
import net.minecraft.world.SpawnDensityCapper;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SpawnHelper.Info.class)
public interface SpawnHelperInfoAccessor {

    @Accessor("densityCapper")
    SpawnDensityCapper getDensityCapper();

    @Invoker("test")
    boolean test(EntityType<?> entityType, BlockPos blockPos, Chunk chunk);

    @Invoker("run")
    void run(MobEntity mobEntity, Chunk chunk);

}
