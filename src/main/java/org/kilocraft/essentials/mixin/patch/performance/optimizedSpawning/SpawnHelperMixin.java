package org.kilocraft.essentials.mixin.patch.performance.optimizedSpawning;

import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import org.kilocraft.essentials.patch.ChunkManager;
import org.kilocraft.essentials.patch.optimizedSpawning.SpawnUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpawnHelper.class)
public abstract class SpawnHelperMixin {
    @Unique
    private static Chunk cachedChunk;
    @Unique
    private static ServerWorld cachedWorld;

    private SpawnHelperMixin() {
    }

    @Shadow
    private static boolean isAcceptableSpawnPosition(ServerWorld serverWorld, Chunk chunk, BlockPos.Mutable mutable, double d) {
        throw new AssertionError();
    }

    @Shadow
    static Biome getBiomeDirectly(BlockPos pos, Chunk chunk) {
        throw new AssertionError();
    }

    // Cancel spawn attempts if they are in unloaded chunks.
    @Redirect(method = "spawnEntitiesInChunk(Lnet/minecraft/entity/SpawnGroup;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/SpawnHelper$Checker;Lnet/minecraft/world/SpawnHelper$Runner;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/SpawnHelper;isAcceptableSpawnPosition(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/util/math/BlockPos$Mutable;D)Z"))
    private static boolean onlySpawnIfLoaded(ServerWorld world, Chunk chunk, BlockPos.Mutable pos, double squaredDistance) {
        return isAcceptableSpawnPosition(world, chunk, pos, squaredDistance) && (cachedChunk = ChunkManager.getChunkIfLoaded(world, pos)) != null;
    }

    // Fast biome lookups.
    @Redirect(method = "getSpawnEntries", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getBiome(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/world/biome/Biome;"))
    private static Biome fastBiomeLookup$1(ServerWorld serverWorld, BlockPos pos) {
        return getBiomeDirectly(pos, cachedChunk);
    }

    @Redirect(method = "pickRandomSpawnEntry", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getBiome(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/world/biome/Biome;"))
    private static Biome fastBiomeLookup$2(ServerWorld serverWorld, BlockPos pos) {
        return getBiomeDirectly(pos, cachedChunk);
    }

    @Inject(method = "spawn", at = @At("HEAD"))
    private static void captureSpawn(ServerWorld world, WorldChunk chunk, SpawnHelper.Info info, boolean spawnAnimals, boolean spawnMonsters, boolean rareSpawn, CallbackInfo ci) {
        cachedWorld = world;
    }

    @Redirect(method = "spawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/SpawnHelper$Info;isBelowCap(Lnet/minecraft/entity/SpawnGroup;Lnet/minecraft/util/math/ChunkPos;)Z"))
    private static boolean isBelowCap(SpawnHelper.Info info, SpawnGroup group, ChunkPos chunkPos) {
        int cap = SpawnUtil.getGlobalMobCap(info, cachedWorld, group);
        if (info.getGroupToCount().getInt(group) >= cap) {
            return false;
        } else {
            return ((SpawnHelperInfoAccessor) info).getDensityCapper().canSpawn(group, chunkPos);
        }
    }

}
