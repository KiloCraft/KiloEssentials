package org.kilocraft.essentials.api.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class RelativePosition {
    private final double x;
    private final double y;
    private final double z;

    public RelativePosition(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public BlockPos getRelativeBlockPos(BlockPos pos) {
        return new BlockPos(pos.getX() + this.x, pos.getY() + this.y, pos.getZ() + this.z);
    }

    public Vec3 getRelativeVector(Vec3 vec) {
        return new Vec3(vec.x() + this.x, vec.y() + this.y, vec.z() + this.z);
    }

}
