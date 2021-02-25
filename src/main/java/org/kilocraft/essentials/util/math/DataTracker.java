package org.kilocraft.essentials.util.math;

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

    public static void compute() {
        for (Average average: averages) {
            average.compute();
        }
    }

}
