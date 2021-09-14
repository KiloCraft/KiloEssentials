package org.kilocraft.essentials.mixin.patch.performance.perPlayerMobcap;

import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SpawnHelper.class)
public interface SpawnHelperAccessor {

    @Accessor("CHUNK_AREA")
    static int getChunkArea() {
        throw new AssertionError();
    }

    @Invoker("canSpawn")
    static boolean canSpawn(ServerWorld serverWorld, SpawnGroup spawnGroup, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, SpawnSettings.SpawnEntry spawnEntry, BlockPos.Mutable mutable, double d) {
        throw new AssertionError();
    }

}
