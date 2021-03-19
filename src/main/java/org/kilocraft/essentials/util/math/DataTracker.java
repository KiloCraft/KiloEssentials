package org.kilocraft.essentials.util.math;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.ModConstants;
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
    public static final Average tps = new Average(28800);
    private static final Average[] averages = {spawnAttempts, tickedChunks, tickedEntities, tickedBlockEntities, cSpawnAttempts, cTickedChunks, cTickedEntities, cTickedBlockEntities};

    public static void compute() {
        for (Average average : averages) {
            average.compute();
        }
    }

    public static double getMSPT() {
        MinecraftServer server = KiloEssentials.getServer().getMinecraftServer();
        Average a = new Average(100).setData(server.lastTickLengths).setIndex(server.getTicks());
        return a.getAverage() / (1000000L);
    }

    public static String getFormattedMSPT() {
        MinecraftServer server = KiloEssentials.getServer().getMinecraftServer();
        Average a = new Average(100).setData(server.lastTickLengths).setIndex(server.getTicks());
        return ModConstants.DECIMAL_FORMAT.format(a.getAverage() / (1000000L));
    }

}
