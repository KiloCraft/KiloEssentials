package org.kilocraft.essentials.mixin;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.kilocraft.essentials.util.math.DataTracker;
import org.kilocraft.essentials.util.settings.ServerSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;

@Mixin(SpawnHelper.class)
public abstract class SpawnHelperMixin {

    @Inject(method = "spawnEntitiesInChunk(Lnet/minecraft/entity/SpawnGroup;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/WorldChunk;Lnet/minecraft/world/SpawnHelper$Checker;Lnet/minecraft/world/SpawnHelper$Runner;)V", at = @At(value = "HEAD"), cancellable = true)
    private static void ke$adjustMobcap(SpawnGroup spawnGroup, ServerWorld world, WorldChunk chunk, SpawnHelper.Checker checker, SpawnHelper.Runner runner, CallbackInfo ci) {
        int tickDistance = ServerSettings.getInt("tick.distance");
        if (tickDistance != -1) {
            Entity player = world.getClosestPlayer(chunk.getPos().getStartX() + 8, 128, chunk.getPos().getStartZ() + 8, -1.0D, false);
            if (player != null) {
                if (chunk.getPos().getChebyshevDistance(player.getChunkPos()) > tickDistance) {
                    DataTracker.cSpawnAttempts.track();
                    ci.cancel();
                    return;
                }
            }
        }
        DataTracker.spawnAttempts.track();
        if (!ServerSettings.perPlayerMobcap) return;
        PlayerEntity player = world.getClosestPlayer(chunk.getPos().getStartX() + 8, 128, chunk.getPos().getStartZ() + 8, -1.0D, false);
        if (player == null) return;
        if (DataTracker.cachedEntityCount.containsKey(world)) {
            HashMap<ServerPlayerEntity, Object2IntOpenHashMap<SpawnGroup>> map = DataTracker.cachedEntityCount.get(world);
            if (map.containsKey(player)) {
                Object2IntOpenHashMap<SpawnGroup> map1 = map.get(player);
                if (map1.containsKey(spawnGroup)) {
                    int i = map1.getInt(spawnGroup);
                    if (i > spawnGroup.getCapacity() * ServerSettings.perPlayerMobcapMax) {
                        ci.cancel();
                    }
                }
            }
        }

    }

    @Inject(method = "canSpawn(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/SpawnGroup;Lnet/minecraft/world/gen/StructureAccessor;Lnet/minecraft/world/gen/chunk/ChunkGenerator;Lnet/minecraft/world/biome/SpawnSettings$SpawnEntry;Lnet/minecraft/util/math/BlockPos$Mutable;D)Z", at = @At(value = "HEAD"), cancellable = true)
    private static void canEntitySpawn(ServerWorld serverWorld, SpawnGroup spawnGroup, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, SpawnSettings.SpawnEntry spawnEntry, BlockPos.Mutable mutable, double d, CallbackInfoReturnable<Boolean> cir) {
        if (!ServerSettings.entityTickCache[Registry.ENTITY_TYPE.getRawId(spawnEntry.type) + 1]) cir.setReturnValue(false);
    }

    @Inject(method = "spawn", at = @At(value = "HEAD"), cancellable = true)
    private static void canEntitiesSpawn(ServerWorld serverWorld, WorldChunk worldChunk, SpawnHelper.Info info, boolean bl, boolean bl2, boolean bl3, CallbackInfo ci) {
        if (!ServerSettings.entityTickCache[0]) ci.cancel();
    }
}
