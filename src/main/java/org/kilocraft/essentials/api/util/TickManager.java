package org.kilocraft.essentials.api.util;

import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.ModConstants;

import java.util.concurrent.TimeUnit;

public class TickManager {

    //Ticks worth one real day
    private final static int STORED_TICKS = 72000;
    //Cached tick times for mspt and tps calculations
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

    public static void onTick() {
        currentTick = KiloEssentials.getMinecraftServer().getTicks();
        //Get the tick length of the previous
        long[] lastTickLengths = KiloEssentials.getMinecraftServer().lastTickLengths;
        long lastTickLength = lastTickLengths[(currentTick + lastTickLengths.length - 1) % lastTickLengths.length];
        //Make sure the value was initialized
        if (lastTickLength != 0) {
            TICK_TIMES[currentTick % STORED_TICKS] = lastTickLength;
        }
        calculateTps();
    }

    private static void calculateTps() {
        for (int i = 0; i < TICK_STORAGE_SIZES.length; i++) {
            writeTpsAndMspt(i);
        }
    }

    private static void writeTpsAndMspt(int index) {
        //Time used for calculating ticks per second (each tick is at least 50ms long)
        double totalTickTime = 0;
        //Time used for calculating average ms per tick
        double actualTotalTickTime = 0;
        int validTicks = 0;
        for (int i = 0; i < Math.min(TICK_STORAGE_SIZES[index], STORED_TICKS); i++) {
            long tickTime = TICK_TIMES[(currentTick - i + STORED_TICKS) % STORED_TICKS];
            if (tickTime != 0) {
                //Calculate tick length (has to be at least 50ms, because that is how long the server will wait if it finished quicker)
                totalTickTime += Math.max(tickTime, TimeUnit.MILLISECONDS.toNanos(50));
                actualTotalTickTime += tickTime;
                validTicks++;
            }
        }
        if (validTicks > 0) {
            double averageTickLength = actualTotalTickTime / validTicks;
            double averageTPS = TimeUnit.SECONDS.toNanos(1) / (totalTickTime / validTicks);
            tps[index] = averageTPS;
            mspt[index] = averageTickLength / 1000000;
        }
    }

    public static String getFormattedMSPT() {
        return ModConstants.DECIMAL_FORMAT.format(mspt[0]);
    }

}
