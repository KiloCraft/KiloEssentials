package org.kilocraft.essentials.mixin.ppm;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.chunk.ChunkManager;
import org.kilocraft.essentials.util.perPlayerMobSpawn.PlayerMobDistanceMap;
import org.kilocraft.essentials.util.perPlayerMobSpawn.ServerPlayerEntityInterface;
import org.kilocraft.essentials.util.perPlayerMobSpawn.ThreadedAnvilChunkStorageInterface;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Arrays;

@Mixin(ServerChunkManager.class)
public abstract class ServerChunkManagerMixin extends ChunkManager {

    //per-player-mobspawn paper patch

    @Shadow
    @Final
    public ThreadedAnvilChunkStorage threadedAnvilChunkStorage;
    @Shadow
    @Final
    private ChunkTicketManager ticketManager;
    @Shadow
    @Final
    private ServerWorld world;

    @Redirect(method = "tickChunks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/SpawnHelper;setupSpawn(ILjava/lang/Iterable;Lnet/minecraft/world/SpawnHelper$ChunkSource;)Lnet/minecraft/world/SpawnHelper$Info;"))
    public SpawnHelper.Info updateSpawnHelper(int i, Iterable<Entity> iterable, SpawnHelper.ChunkSource chunkSource) {
        SpawnHelper.Info spawnHelperInfo;
        PlayerMobDistanceMap mobDistanceMap = ((ThreadedAnvilChunkStorageInterface) threadedAnvilChunkStorage).getMobDistanceMap();
        if (mobDistanceMap != null) {
            // update distance map
            mobDistanceMap.update(this.world.getPlayers(), ((ThreadedAnvilChunkStorageAccessor) threadedAnvilChunkStorage).getWatchDistance());
            // re-set mob counts
            for (ServerPlayerEntity player : this.world.getPlayers()) {
                Arrays.fill(((ServerPlayerEntityInterface) player).getMobCounts(), 0);
            }
            spawnHelperInfo = setupSpawn(i, iterable, chunkSource, true);
        } else {
            spawnHelperInfo = setupSpawn(i, iterable, chunkSource, false);
        }
        return spawnHelperInfo;
    }

    public SpawnHelper.Info setupSpawn(int i, Iterable<Entity> iterable, SpawnHelper.ChunkSource chunkSource, boolean countMobs) {
        if (countMobs) {
            iterable.forEach(entity -> {
                ((ThreadedAnvilChunkStorageInterface)world.getChunkManager().threadedAnvilChunkStorage).updatePlayerMobTypeMap(entity);
            });
        }
        return SpawnHelper.setupSpawn(i, iterable, chunkSource);
    }

}
