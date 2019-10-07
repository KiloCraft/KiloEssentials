package org.kilocraft.essentials.api.math;

public class MathHelper {

    public static float sin(float float_1) {
        return (float) Math.sin(float_1);
    }

    public static float cos(float float_1) {
        return (float) Math.cos(float_1);
    }

    public static float sqrt(double float_1) {
        return (float) Math.sqrt(float_1);
    }

    public static int floor(float float_1) {
        int int_1 = (int)float_1;
        return float_1 < (float)int_1 ? int_1 - 1 : int_1;
    }
}
