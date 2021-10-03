package org.kilocraft.essentials.mixin.patch.performance.perPlayerMobcap;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SpawnHelper.Info.class)
public interface SpawnHelperInfoAccessor {


    @Accessor("spawningChunkCount")
    int getSpawnChunkCount();

    @Accessor("groupToCountView")
    Object2IntMap<SpawnGroup> getGroupToCountView();

    @Invoker("test")
    boolean test(EntityType<?> entityType, BlockPos blockPos, Chunk chunk);

    @Invoker("run")
    void run(MobEntity mobEntity, Chunk chunk);

    @Invoker("isBelowCap")
    boolean isBelowCap(SpawnGroup spawnGroup);

}
