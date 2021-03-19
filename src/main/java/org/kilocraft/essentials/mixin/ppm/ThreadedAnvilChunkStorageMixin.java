package org.kilocraft.essentials.mixin.ppm;

import com.mojang.datafixers.DataFixer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.structure.StructureManager;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.ChunkStatusChangeListener;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.storage.LevelStorage;
import org.kilocraft.essentials.util.perPlayerMobSpawn.PlayerMobDistanceMap;
import org.kilocraft.essentials.util.perPlayerMobSpawn.ServerPlayerEntityInterface;
import org.kilocraft.essentials.util.perPlayerMobSpawn.ThreadedAnvilChunkStorageInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

@Mixin(ThreadedAnvilChunkStorage.class)
public class ThreadedAnvilChunkStorageMixin implements ThreadedAnvilChunkStorageInterface {

    PlayerMobDistanceMap playerMobDistanceMap = null;

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    public void onInit(ServerWorld serverWorld, LevelStorage.Session session, DataFixer dataFixer, StructureManager structureManager, Executor executor, ThreadExecutor<Runnable> threadExecutor, ChunkProvider chunkProvider, ChunkGenerator chunkGenerator, WorldGenerationProgressListener worldGenerationProgressListener, ChunkStatusChangeListener chunkStatusChangeListener, Supplier<PersistentStateManager> supplier, int i, boolean bl, CallbackInfo ci) {
        this.playerMobDistanceMap = new PlayerMobDistanceMap();
    }

    @Override
    public PlayerMobDistanceMap getMobDistanceMap() {
        return playerMobDistanceMap;
    }

    public void updatePlayerMobTypeMap(Entity entity) {
        //TODO:
        if (!true/*this.world.paperConfig.perPlayerMobSpawns*/) {
            return;
        }
        int chunkX = (int) Math.floor(entity.getPos().getX()) >> 4;
        int chunkZ = (int) Math.floor(entity.getPos().getZ()) >> 4;
        int index = entity.getType().getSpawnGroup().ordinal();

        for (ServerPlayerEntity player : this.playerMobDistanceMap.getPlayersInRange(chunkX, chunkZ)) {
            ++((ServerPlayerEntityInterface)player).getMobCounts()[index];
        }
    }

    @Override
    public int getMobCountNear(ServerPlayerEntity player, SpawnGroup spawnGroup) {
        return ((ServerPlayerEntityInterface)player).getMobCounts()[spawnGroup.ordinal()];
    }
}
