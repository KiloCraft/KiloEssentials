package org.kilocraft.essentials.util.math;

public class Average {

    private long[] data;
    private int index = 0;
    private long tracked = 0;
    public Average(int size) {
        data = new long[size];
    }

    public static String format(double d) {
        return String.format("%.2f", d);
    }

    public Average setData(long[] data) {
        this.data = data;
        return this;
    }

    public Average setIndex(int index) {
        this.index = index;
        return this;
    }

    public void add(long l) {
        data[index % data.length] = l;
        index++;
    }

    public void track() {
        tracked++;
    }

    protected void compute() {
        add(tracked);
        tracked = 0;
    }

    public double getAverage() {
        double result = 0;
        int to = Math.min(data.length, index);
        for (int i = 0; i < to; i++) {
            result += data[i];
        }
        return result / to;
    }

    /*
     * Get the average of the last x entries
     */
    public double getAverage(int x) {
        x = Math.min(x, index);
        double result = 0;
        for (int i = index - 1; i >= index - x; i--) {
            result += data[i < 0 ? data.length - i : i % data.length];
        }
        return result / x;
    }

    public String formattedAverage() {
        return format(getAverage());
    }

    public String formattedAverage(int x) {
        return format(getAverage(x));
    }

    public String toString() {
        StringBuilder result = new StringBuilder("[");
        int to = Math.min(data.length, index);
        for (int i = 0; i < data.length; i++) {
            if (index - 1 == i) result.append("(");
            result.append(i >= to ? "X" : data[i]);
            if (index - 1 == i) result.append(")");
            if (i != data.length - 1) result.append(",");
        }
        result.append("]");
        return result.toString();
    }

}
