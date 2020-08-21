package org.kilocraft.essentials.api.world;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class RelativePosition {
    private final double x;
    private final double y;
    private final double z;

    public RelativePosition(final double x, final double y, final double z) {
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
        return new BlockPos(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
    }

    public Vec3d getRelativeVector(Vec3d vec) {
        return new Vec3d(vec.getX() + x, vec.getY() + y, vec.getZ() + z);
    }

}
