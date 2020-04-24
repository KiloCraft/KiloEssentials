package org.kilocraft.essentials.util;

import org.kilocraft.essentials.util.math.RollingAverage;

public class TPSTracker {
    public static final RollingAverage tps1 = new RollingAverage(60);
    public static final RollingAverage tps5 = new RollingAverage(60 * 5);
    public static final RollingAverage tps15 = new RollingAverage(60 * 15);
    public static final RollingAverage tps30 = new RollingAverage(60 * 30);
    public static final RollingAverage tps60 = new RollingAverage(60 * 60);
}
