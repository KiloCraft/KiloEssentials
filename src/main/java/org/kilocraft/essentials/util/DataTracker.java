package org.kilocraft.essentials.util;

import org.kilocraft.essentials.util.math.Average;

import java.util.HashMap;
import java.util.Map;

public class DataTracker {

    public static final Average spawnAttempts = new Average(60);
    public static final Average tickedChunks = new Average(60);
    public static final Average tickedEntities = new Average(60);
    public static final Average tickedBlockEntities = new Average(60);
    public static final Average cSpawnAttempts = new Average(60);
    public static final Average cTickedChunks = new Average(60);
    public static final Average cTickedEntities = new Average(60);
    public static final Average cTickedBlockEntities = new Average(60);
    public static final HashMap<Average, Long> averages = new HashMap<>();

    public static void compute() {
        for (Map.Entry<Average, Long> entry : averages.entrySet()) {
            entry.getKey().add(entry.getValue());
            averages.put(entry.getKey(), 0L);
        }
    }

    public static void add(Average average) {
        long count = averages.getOrDefault(average, 0L);
        averages.put(average, count + 1);
    }

}
