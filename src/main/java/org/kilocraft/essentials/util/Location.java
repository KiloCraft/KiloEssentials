package org.kilocraft.essentials.util;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.user.OnlineUser;

import static org.kilocraft.essentials.api.KiloServer.getServer;

public class Location {
    public static int MAX_BUILD_LIMIT = KiloServer.getServer().getVanillaServer().getWorldHeight();
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
        return LocationUtil.isBlockSafe(this);
    }

    public boolean isSafeFor(OnlineUser user) {
        return LocationUtil.isBlockSafeFor(user, this);
    }

    @Nullable
    public BlockPos getPosOnGround() {
        return LocationUtil.getPosOnGround(this);
    }

    public void setPos(BlockPos pos) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
    }

    public void setPos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setDimension(Identifier dimension) {
        this.dimension = dimension;
    }

    public void setDimension(DimensionType dimension) {
        this.dimension = Registry.DIMENSION.getId(dimension);
    }

    public void setView(double yaw, double pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

}
