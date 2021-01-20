package org.kilocraft.essentials.util.math;

public class Average {

    public final long[] data;
    private int index = 0;

    public Average(int size) {
        this.data = new long[size];
    }

    public void add(long l) {
        data[index % data.length] = l;
        index++;
    }


    public double getAverage() {
        double result = 0;
        for (long l : data) {
            result += l;
        }
        return result / data.length;
    }

    public String formattedAverage() {
        return String.format("%.2f", getAverage());
    }

}
