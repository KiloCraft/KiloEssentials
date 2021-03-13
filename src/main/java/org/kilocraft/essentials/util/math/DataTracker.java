package org.kilocraft.essentials.util.math;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.util.settings.ServerSettings;

import java.util.HashMap;

public class DataTracker {

    public static final Average spawnAttempts = new Average(60);
    public static final Average tickedChunks = new Average(60);
    public static final Average tickedEntities = new Average(60);
    public static final Average tickedBlockEntities = new Average(60);
    public static final Average cSpawnAttempts = new Average(60);
    public static final Average cTickedChunks = new Average(60);
    public static final Average cTickedEntities = new Average(60);
    public static final Average cTickedBlockEntities = new Average(60);
    private static final Average[] averages = {spawnAttempts, tickedChunks, tickedEntities, tickedBlockEntities, cSpawnAttempts, cTickedChunks, cTickedEntities, cTickedBlockEntities};
    public static final HashMap<ServerWorld, HashMap<ServerPlayerEntity, Object2IntOpenHashMap<SpawnGroup>>> cachedEntityCount = new HashMap<>();


    public static void compute() {
        for (Average average: averages) {
            average.compute();
        }
    }

    public static void computeEntityCache() {
        cachedEntityCount.clear();
        int viewDistance = ServerSettings.getInt("view_distance");
        for (ServerWorld world : KiloEssentials.getServer().getWorlds()) {
            HashMap<ServerPlayerEntity, Object2IntOpenHashMap<SpawnGroup>> playerToSpawnGroup = new HashMap<>();
            for (Entity entity : world.iterateEntities()) {
                for (ServerPlayerEntity player : world.getPlayers()) {
                    if (player.getChunkPos().getChebyshevDistance(entity.getChunkPos()) <= viewDistance && entity.getEntityWorld().equals(player.getEntityWorld())) {
                        Object2IntOpenHashMap<SpawnGroup> intOpenHashMap = playerToSpawnGroup.getOrDefault(player, new Object2IntOpenHashMap<>());
                        intOpenHashMap.addTo(entity.getType().getSpawnGroup(), 1);
                        playerToSpawnGroup.put(player, intOpenHashMap);
                    }
                }
            }
            cachedEntityCount.put(world, playerToSpawnGroup);
        }
    }

}
