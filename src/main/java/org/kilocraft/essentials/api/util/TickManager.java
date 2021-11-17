package org.kilocraft.essentials.api.util;

import net.minecraft.entity.EntityType;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameMode;
import net.minecraft.world.WorldAccess;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.util.settings.ServerSettings;

public class TickManager {

    // Ticks for one real day
    private final static int STORED_TICKS = 72000;
    // Cached tick times for mspt and tps calculations
    private static final long[] TICK_TIMES = new long[STORED_TICKS];
    /*
     * Arrays that store the tps & mspt of the last x seconds
     * index - time
     * 0 - 5s
     * 1 - 15s
     * 2 - 1m
     * 3 - 5m
     * 4 - 15m
     * 5 - 1h
     * */
    private final static int[] TICK_STORAGE_SIZES = {100, 300, 1200, 6000, 18000, 72000};
    public static double[] tps = new double[TICK_STORAGE_SIZES.length];
    public static double[] mspt = new double[TICK_STORAGE_SIZES.length];
    private static int currentTick = 0;

    private TickManager() {
    }

    public static void onTick() {
        currentTick = KiloEssentials.getMinecraftServer().getTicks();
        // Get the tick length of the previous tick
        long[] lastTickLengths = KiloEssentials.getMinecraftServer().lastTickLengths;
        long lastTickLength = lastTickLengths[(currentTick + lastTickLengths.length - 1) % lastTickLengths.length];
        // Make sure the value was initialized
        if (lastTickLength != 0) {
            TICK_TIMES[currentTick % STORED_TICKS] = lastTickLength;
        }
        calculateTps();
    }

    private static void calculateTps() {
        for (int i = 0; i < TICK_STORAGE_SIZES.length; i++) {
            if (i == 0 || currentTick % (i * 5) == 0) writeTpsAndMspt(i);
        }
    }

    private static void writeTpsAndMspt(int index) {
        // Time used for calculating ticks per second (each tick is at least 50ms long)
        double totalTickTime = 0;
        // Time used for calculating average ms per tick
        double actualTotalTickTime = 0;
        int validTicks = 0;
        int length = Math.min(TICK_STORAGE_SIZES[index], STORED_TICKS);
        for (int i = 0; i < length; i++) {
            long tickTime = TICK_TIMES[(currentTick - i + STORED_TICKS) % STORED_TICKS];
            if (tickTime != 0) {
                // Calculate tick length (has to be at least 50ms, because that is how long the server will wait if it finished quicker)
                totalTickTime += Math.max(tickTime, 50000000);
                actualTotalTickTime += tickTime;
                validTicks++;
            }
        }
        if (validTicks > 0) {
            double averageTickLength = actualTotalTickTime / validTicks;
            double averageTPS = 1000000000 / (totalTickTime / validTicks);
            tps[index] = averageTPS;
            mspt[index] = averageTickLength / 1000000;
        }
    }

    public static boolean isEntityLimitReached(WorldAccess world, BlockPos pos, String id, EntityType<?>... entityType) {
        if (entityType == null || entityType.length == 0) return true;
        int range = ServerSettings.getInt("entity_limit." + id + ".range");
        int limit = ServerSettings.getInt("entity_limit." + id + ".limit");
        // Ignore negative values
        if (range > 0 && limit > 0) {
            // Count mobs from all given types
            int entityCount = 0;
            for (EntityType<?> type : entityType) {
                entityCount += world.getEntitiesByType(type, new Box(pos.mutableCopy().add(range, range, range), pos.mutableCopy().add(-range, -range, -range)), EntityPredicates.EXCEPT_SPECTATOR).size();
            }
            return limit <= entityCount;
        }
        return false;
    }

    public static boolean isEntityLimitReached(WorldAccess world, BlockPos pos, EntityType<?>... entityType) {
        return isEntityLimitReached(world, pos, Registry.ENTITY_TYPE.getId(entityType[0]).getPath(), entityType);
    }

    public static String getFormattedMSPT() {
        return ModConstants.DECIMAL_FORMAT.format(mspt[0]);
    }

}
