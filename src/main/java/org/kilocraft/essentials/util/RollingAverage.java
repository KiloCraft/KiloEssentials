package org.kilocraft.essentials.util;

import java.text.DecimalFormat;

public class RollingAverage {
    public static final int SAMPLE_INTERVAL = 20;
    public static final java.math.BigDecimal TPS_BASE = new java.math.BigDecimal(1E9).multiply(new java.math.BigDecimal(SAMPLE_INTERVAL));
    private static final int TPS = 20;
    public static final int TICK_TIME = 1000000000 / TPS;
    private static final long SEC_IN_NANO = 1000000000;
    private static final long MAX_CATCHUP_BUFFER = TICK_TIME * TPS * 60L;
    private final int size;
    private final java.math.BigDecimal[] samples;
    private final long[] times;
    private long time;
    private java.math.BigDecimal total;
    private int index = 0;

    public RollingAverage(int size) {
        this.size = size;
        this.time = size * SEC_IN_NANO;
        this.total = dec(TPS).multiply(dec(SEC_IN_NANO)).multiply(dec(size));
        this.samples = new java.math.BigDecimal[size];
        this.times = new long[size];
        for (int i = 0; i < size; i++) {
            this.samples[i] = dec(TPS);
            this.times[i] = SEC_IN_NANO;
        }
    }

    private static java.math.BigDecimal dec(long t) {
        return new java.math.BigDecimal(t);
    }

    public void add(java.math.BigDecimal x, long t) {
        time -= times[index];
        total = total.subtract(samples[index].multiply(dec(times[index])));
        samples[index] = x;
        times[index] = t;
        time += t;
        total = total.add(x.multiply(dec(t)));
        if (++index == size) {
            index = 0;
        }
    }

    public double getAverage() {
        return total.divide(dec(time), 30, java.math.RoundingMode.HALF_UP).doubleValue();
    }

    public String getShortAverage() {
        DecimalFormat decimalFormat = new DecimalFormat("##.##");
        return decimalFormat.format(getAverage());
    }
}
