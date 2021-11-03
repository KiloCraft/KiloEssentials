package org.kilocraft.essentials.mixin.patch.performance.optimizedSpawning;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.SpawnDensityCapper;
import org.kilocraft.essentials.mixin.patch.performance.ThreadedAnvilChunkStorageAccessor;
import org.kilocraft.essentials.patch.ChunkManager;
import org.kilocraft.essentials.patch.optimizedSpawning.SpawnUtil;
import org.kilocraft.essentials.patch.optimizedSpawning.IThreadedAnvilChunkStorage;
import org.kilocraft.essentials.util.settings.ServerSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

@Mixin(SpawnDensityCapper.class)
public abstract class SpawnDensityCapperMixin {

    @Shadow
    @Final
    private ThreadedAnvilChunkStorage threadedAnvilChunkStorage;

    @Shadow
    @Final
    private Long2ObjectMap<List<ServerPlayerEntity>> chunkPosToMobSpawnablePlayers;

    /**
     * @author Wesley1808
     * @reason Use PlayerMobDistanceMap instead of Mojang's default implementation.
     */
    @Overwrite
    private List<ServerPlayerEntity> getMobSpawnablePlayers(ChunkPos pos) {
        if (ServerSettings.optimizedSpawning) {
            return List.of(); // Return empty list as we won't be using it.
        }

        // Optimizes vanilla code by getting rid of stream allocations.
        final ThreadedAnvilChunkStorageAccessor chunkStorage = (ThreadedAnvilChunkStorageAccessor) this.threadedAnvilChunkStorage;
        return this.chunkPosToMobSpawnablePlayers.computeIfAbsent(pos.toLong(), l -> getNearbyPlayers(chunkStorage.getWorld(), pos, chunkStorage.getPlayerChunkWatchingManager().watchingPlayers.keySet()));
    }

    @Redirect(method = "canSpawn", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"))
    private Iterator<ServerPlayerEntity> usePlayerMobDistanceMap$1(List<ServerPlayerEntity> list, SpawnGroup spawnGroup, ChunkPos chunkPos) {
        return ServerSettings.optimizedSpawning ? ((IThreadedAnvilChunkStorage) this.threadedAnvilChunkStorage).getPlayerMobDistanceMap().getPlayersInRange(chunkPos).iterator() : list.listIterator();
    }

    @Redirect(method = "increaseDensity", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"))
    private Iterator<ServerPlayerEntity> usePlayerMobDistanceMap$2(List<ServerPlayerEntity> list, ChunkPos chunkPos, SpawnGroup spawnGroup) {
        return ServerSettings.optimizedSpawning ? ((IThreadedAnvilChunkStorage) this.threadedAnvilChunkStorage).getPlayerMobDistanceMap().getPlayersInRange(chunkPos).iterator() : list.listIterator();
    }

    @Redirect(method = "canSpawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/SpawnDensityCapper$DensityCap;canSpawn(Lnet/minecraft/entity/SpawnGroup;)Z"))
    public boolean modifySpawnCap(SpawnDensityCapper.DensityCap densityCap, SpawnGroup spawnGroup) {
        return densityCap.spawnGroupsToDensity.getOrDefault(spawnGroup, 0) < SpawnUtil.getPersonalMobCap(((ThreadedAnvilChunkStorageAccessor) this.threadedAnvilChunkStorage).getWorld(), spawnGroup);
    }

    private static List<ServerPlayerEntity> getNearbyPlayers(ServerWorld world, ChunkPos pos, ObjectSet<ServerPlayerEntity> watchingPlayers) {
        final ChunkTicketManager manager = ChunkManager.getTicketManager(world);
        if (manager == null || !manager.shouldTick(pos.toLong())) {
            return List.of();
        }

        final Predicate<ServerPlayerEntity> predicate = player -> !player.isSpectator() && getSquaredDistance(pos, player) < 16384.0D;
        return watchingPlayers != null ? getNearbyWatchingPlayers(watchingPlayers, predicate) : world.getPlayers(predicate);
    }

    private static List<ServerPlayerEntity> getNearbyWatchingPlayers(ObjectSet<ServerPlayerEntity> watchingPlayers, Predicate<ServerPlayerEntity> predicate) {
        final List<ServerPlayerEntity> players = new ArrayList<>();
        for (ServerPlayerEntity player : watchingPlayers) {
            if (predicate.test(player)) {
                players.add(player);
            }
        }
        return players;
    }

    private static double getSquaredDistance(ChunkPos pos, Entity entity) {
        final double x = ChunkSectionPos.getOffsetPos(pos.x, 8) - entity.getX();
        final double z = ChunkSectionPos.getOffsetPos(pos.z, 8) - entity.getZ();
        return x * x + z * z;
    }

}
