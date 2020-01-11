package org.kilocraft.essentials.util;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.user.OnlineUser;

import static org.kilocraft.essentials.api.KiloServer.getServer;

public class Location {
    private static int MAX_BUILD_LIMIT = 255;
    private int x, y, z;
    private double yaw, pitch;
    private Identifier dimension;

    public Location(int x, int y, int z, double yaw, double pitch, Identifier dimension) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.dimension = dimension;
    }

    public Location(int x, int y, int z, Identifier dimension) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.dimension = dimension;
    }

    public Location(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static Location of(BlockPos pos) {
        return new Location(pos.getX(), pos.getY(), pos.getZ(), 0, 0, null);
    }

    public static Location of(ServerPlayerEntity player) {
        BlockPos pos = player.getBlockPos();
        return new Location(pos.getX(), pos.getY(), pos.getZ(), player.yaw, player.pitch, Registry.DIMENSION.getId(player.dimension));
    }

    public static Location of(int x, int y, int z, DimensionType dimensionType) {
        return new Location(x, y, z, Registry.DIMENSION.getId(dimensionType));
    }

    public static Location of(OnlineUser user) {
        return Location.of(user.getPlayer());
    }

    public BlockPos getPos() {
        return new BlockPos(this.x, this.y, this.z);
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }

    public double getYaw() {
        return this.yaw;
    }

    public double getPitch() {
        return this.pitch;
    }

    public Identifier getDimensionId() {
        return this.dimension;
    }

    public DimensionType getDimension() {
        return Registry.DIMENSION.get(this.dimension);
    }

    public ServerWorld getWorld() {
        return getServer().getVanillaServer().getWorld(this.getDimension());
    }

    public boolean isSafe() {
        if (this.dimension == null)
            return false;

        boolean safe = false;

        for (int i = 0; i < 4; i++) {
            BlockPos pos = new BlockPos(this.x, i, this.z);
            if (this.getWorld().getBlockState(pos).isOpaque()) {
                safe = true;
                break;
            }
        }

        return safe;
    }

    @Nullable
    public BlockPos getPosOnGround() {
        if (this.dimension == null)
            return null;

        BlockPos finalPos = this.getPos();
        int blocksLeft = MAX_BUILD_LIMIT - this.y;

        for (int i = 0; i < blocksLeft; i++) {
            BlockPos pos = new BlockPos(this.x, i, this.z);
            if (this.getWorld().getBlockState(pos).isAir())
                continue;

            finalPos = pos;
        }

        return finalPos;
    }

}
