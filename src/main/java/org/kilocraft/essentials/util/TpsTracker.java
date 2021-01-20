package org.kilocraft.essentials.util;

import org.apache.commons.lang3.time.StopWatch;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.util.math.RollingAverage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TpsTracker {
    public static final RollingAverage tps = new RollingAverage(60);
    public static final RollingAverage tps5 = new RollingAverage(60 * 5);
    public static final RollingAverage tps15 = new RollingAverage(60 * 15);
    public static final RollingAverage tps60 = new RollingAverage(60 * 60);
    public static final RollingAverage tps1440 = new RollingAverage(60 * 1440);


    public static class MillisecondPerTick {
        private static final List<Long> longs = new ArrayList<>();
        private static int tick = 0;
        private static long lastMSPT = 0;
        private static StopWatch watch;

        public static void onStart() {
            watch = new StopWatch();
            watch.start();
        }

        public static void onEnd() {
            watch.stop();
            longs.add(watch.getTime(TimeUnit.MILLISECONDS));

            long total = 0;
            for (Long aLong : longs) {
                total += aLong;
            }

            lastMSPT = total / 20;

            if (tick > 20) {
                longs.clear();
                tick = 0;
            }
            tick++;
        }

        public static long getAverage() {
            return lastMSPT;
        }

        public static String getShortAverage() {
            return ModConstants.DECIMAL_FORMAT.format(getAverage());
        }

    }
}
