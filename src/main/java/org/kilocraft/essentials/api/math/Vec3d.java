package org.kilocraft.essentials.api.math;

public class Vec3d {
    public static final Vec3d ZERO = new Vec3d(0.0D, 0.0D, 0.0D);
    public final double x;
    public final double y;
    public final double z;

    public Vec3d(double double_1, double double_2, double double_3) {
        this.x = double_1;
        this.y = double_2;
        this.z = double_3;
    }

    public static Vec3d fromPolar(float float_1, float float_2) {
        float float_3 = MathHelper.cos(-float_2 * 0.017453292F - 3.1415927F);
        float float_4 = MathHelper.sin(-float_2 * 0.017453292F - 3.1415927F);
        float float_5 = -MathHelper.cos(-float_1 * 0.017453292F);
        float float_6 = MathHelper.sin(-float_1 * 0.017453292F);
        return new Vec3d((double) (float_4 * float_5), (double) float_6, (double) (float_3 * float_5));
    }

    public Vec3d reverseSubtract(Vec3d vec3d_1) {
        return new Vec3d(vec3d_1.x - this.x, vec3d_1.y - this.y, vec3d_1.z - this.z);
    }

    public Vec3d normalize() {
        double double_1 = (double) MathHelper.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
        return double_1 < 1.0E-4D ? ZERO : new Vec3d(this.x / double_1, this.y / double_1, this.z / double_1);
    }

    public double dotProduct(Vec3d vec3d_1) {
        return this.x * vec3d_1.x + this.y * vec3d_1.y + this.z * vec3d_1.z;
    }

    public Vec3d crossProduct(Vec3d vec3d_1) {
        return new Vec3d(this.y * vec3d_1.z - this.z * vec3d_1.y, this.z * vec3d_1.x - this.x * vec3d_1.z, this.x * vec3d_1.y - this.y * vec3d_1.x);
    }

    public Vec3d subtract(Vec3d vec3d_1) {
        return this.subtract(vec3d_1.x, vec3d_1.y, vec3d_1.z);
    }

    public Vec3d subtract(double double_1, double double_2, double double_3) {
        return this.add(-double_1, -double_2, -double_3);
    }

    public Vec3d add(Vec3d vec3d_1) {
        return this.add(vec3d_1.x, vec3d_1.y, vec3d_1.z);
    }

    public Vec3d add(double double_1, double double_2, double double_3) {
        return new Vec3d(this.x + double_1, this.y + double_2, this.z + double_3);
    }

    public double distanceTo(Vec3d vec3d_1) {
        double double_1 = vec3d_1.x - this.x;
        double double_2 = vec3d_1.y - this.y;
        double double_3 = vec3d_1.z - this.z;
        return (double) MathHelper.sqrt(double_1 * double_1 + double_2 * double_2 + double_3 * double_3);
    }

    public double squaredDistanceTo(Vec3d vec3d_1) {
        double double_1 = vec3d_1.x - this.x;
        double double_2 = vec3d_1.y - this.y;
        double double_3 = vec3d_1.z - this.z;
        return double_1 * double_1 + double_2 * double_2 + double_3 * double_3;
    }

    public double squaredDistanceTo(double double_1, double double_2, double double_3) {
        double double_4 = double_1 - this.x;
        double double_5 = double_2 - this.y;
        double double_6 = double_3 - this.z;
        return double_4 * double_4 + double_5 * double_5 + double_6 * double_6;
    }

    public Vec3d multiply(double double_1) {
        return this.multiply(double_1, double_1, double_1);
    }

    public Vec3d negate() {
        return this.multiply(-1.0D);
    }

    public Vec3d multiply(Vec3d vec3d_1) {
        return this.multiply(vec3d_1.x, vec3d_1.y, vec3d_1.z);
    }

    public Vec3d multiply(double double_1, double double_2, double double_3) {
        return new Vec3d(this.x * double_1, this.y * double_2, this.z * double_3);
    }

    public double length() {
        return (double) MathHelper.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }

    public double lengthSquared() {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    public boolean equals(Object object_1) {
        if (this == object_1) {
            return true;
        } else if (!(object_1 instanceof Vec3d)) {
            return false;
        } else {
            Vec3d vec3d_1 = (Vec3d) object_1;
            if (Double.compare(vec3d_1.x, this.x) != 0) {
                return false;
            } else if (Double.compare(vec3d_1.y, this.y) != 0) {
                return false;
            } else {
                return Double.compare(vec3d_1.z, this.z) == 0;
            }
        }
    }

    public int hashCode() {
        long long_1 = Double.doubleToLongBits(this.x);
        int int_1 = (int) (long_1 ^ long_1 >>> 32);
        long_1 = Double.doubleToLongBits(this.y);
        int_1 = 31 * int_1 + (int) (long_1 ^ long_1 >>> 32);
        long_1 = Double.doubleToLongBits(this.z);
        int_1 = 31 * int_1 + (int) (long_1 ^ long_1 >>> 32);
        return int_1;
    }

    public String toString() {
        return "(" + this.x + ", " + this.y + ", " + this.z + ")";
    }

    public Vec3d rotateX(float float_1) {
        float float_2 = MathHelper.cos(float_1);
        float float_3 = MathHelper.sin(float_1);
        double double_1 = this.x;
        double double_2 = this.y * (double) float_2 + this.z * (double) float_3;
        double double_3 = this.z * (double) float_2 - this.y * (double) float_3;
        return new Vec3d(double_1, double_2, double_3);
    }

    public Vec3d rotateY(float float_1) {
        float float_2 = MathHelper.cos(float_1);
        float float_3 = MathHelper.sin(float_1);
        double double_1 = this.x * (double) float_2 + this.z * (double) float_3;
        double double_2 = this.y;
        double double_3 = this.z * (double) float_2 - this.x * (double) float_3;
        return new Vec3d(double_1, double_2, double_3);
    }

    public final double getX() {
        return this.x;
    }

    public final double getY() {
        return this.y;
    }

    public final double getZ() {
        return this.z;
    }

}
