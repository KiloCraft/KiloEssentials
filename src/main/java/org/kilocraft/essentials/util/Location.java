package org.kilocraft.essentials.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.user.OnlineUser;

import java.text.DecimalFormat;

import static org.kilocraft.essentials.api.KiloServer.getServer;

public class Location {
    public static int MAX_BUILD_LIMIT = KiloServer.getServer().getVanillaServer().getWorldHeight();
    private int x, y, z;
    private double xx, yy, zz;
    private float yaw, pitch;
    private Identifier dimension;
    private boolean isVector = false;
    private boolean useShortDecimals = false;
    private DecimalFormat decimalFormat = new DecimalFormat("##.##");

    public Location(int x, int y, int z, float yaw, float pitch, Identifier dimension) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.dimension = dimension;
    }

    public Location(double x, double y, double z, float yaw, float pitch, Identifier dimension) {
        this.xx = x;
        this.yy = y;
        this.zz = z;
        this.yaw = Float.parseFloat(decimalFormat.format(yaw));
        this.pitch = Float.parseFloat(decimalFormat.format(pitch));
        this.dimension = dimension;
        this.isVector = true;
    }

    public Location(int x, int y, int z, Identifier dimension) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.dimension = dimension;
    }

    public Location(double x, double y, double z, Identifier dimension) {
        this.xx = x;
        this.yy = y;
        this.zz = z;
        this.dimension = dimension;
        this.isVector = true;
    }

    public Location(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Location(double x, double y, double z) {
        this.xx = x;
        this.yy = y;
        this.zz = z;
        this.isVector = true;
    }

    public static Location dummy() {
        return new Location(0, 0, 0);
    }

    public static Location dummyOfDouble() {
        return new Location(0.0D, 0.0D, 0.0D);
    }

    public static Location of(BlockPos pos) {
        return new Location(pos.getX(), pos.getY(), pos.getZ(), 0, 0, null);
    }

    public static Location of(ServerPlayerEntity player) {
        BlockPos pos = player.getBlockPos();
        return new Location(pos.getX(), pos.getY(), pos.getZ(), player.yaw, player.pitch, Registry.DIMENSION_TYPE.getId(player.dimension));
    }

    public static Location ofDouble(ServerPlayerEntity player) {
        return new Location(player.getX(), player.getY(), player.getZ(), player.yaw, player.pitch, Registry.DIMENSION_TYPE.getId(player.dimension));
    }

    public static Location of(int x, int y, int z, DimensionType dimensionType) {
        return new Location(x, y, z, Registry.DIMENSION_TYPE.getId(dimensionType));
    }

    public static Location ofDouble(double x, double y, double z, DimensionType dimensionType) {
        return new Location(x, y, z, Registry.DIMENSION_TYPE.getId(dimensionType));
    }

    public static Location of(OnlineUser user) {
        return Location.of(user.getPlayer());
    }

    public static Location ofDouble(OnlineUser user) {
        return Location.ofDouble(user.getPlayer());
    }

    public BlockPos getPos() {
        return new BlockPos(this.x, this.y, this.z);
    }

    public Vec3i getVectorPos() {
        return new Vec3i(this.xx, this.yy, this.zz);
    }

    public int getX() {
        return this.x;
    }

    public double getXx() {
        return this.xx;
    }

    public int getY() {
        return this.y;
    }

    public double getYy() {
        return this.yy;
    }

    public int getZ() {
        return this.z;
    }

    public double getZz() {
        return this.zz;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public Identifier getDimensionId() {
        return this.dimension;
    }

    public DimensionType getDimension() {
        return Registry.DIMENSION_TYPE.get(this.dimension);
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
        this.dimension = Registry.DIMENSION_TYPE.getId(dimension);
    }

    public void setView(float yaw, float pitch) {
        this.yaw = Float.parseFloat(decimalFormat.format(yaw));
        this.pitch = Float.parseFloat(decimalFormat.format(pitch));
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        CompoundTag pos = new CompoundTag();

        if (this.isVector) {
            pos.putDouble("x", this.xx);
            pos.putDouble("y", this.yy);
            pos.putDouble("z", this.zz);
            pos.putBoolean("vector", true);
        } else {
            pos.putInt("x", this.x);
            pos.putInt("y", this.y);
            pos.putInt("z", this.z);
        }

        tag.put("pos", pos);

        if (this.dimension != null)
            tag.putString("dim", this.dimension.toString());

        if (yaw != 0 && pitch != 0) {
            CompoundTag view = new CompoundTag();
            view.putFloat("yaw", this.yaw);
            view.putFloat("pitch", this.pitch);
            tag.put("view", view);
        }

        return tag;
    }

    public void fromTag(CompoundTag compoundTag) {
        CompoundTag pos = compoundTag.getCompound("pos");

        if (pos.contains("vector")) {
            this.xx = pos.getDouble("x");
            this.yy = pos.getDouble("y");
            this.zz = pos.getDouble("z");
            this.isVector = true;
        } else {
            this.x = pos.getInt("x");
            this.y = pos.getInt("y");
            this.z = pos.getInt("z");
        }

        if (compoundTag.contains("dim"))
            this.dimension = new Identifier(compoundTag.getString("dim"));

        if (compoundTag.contains("view")) {
            CompoundTag view = compoundTag.getCompound("view");
            this.yaw = view.getFloat("yaw");
            this.pitch = view.getFloat("pitch");
        }
    }

    public Location shortDecimalForVector() {
        this.useShortDecimals = true;

        DecimalFormat decimalFormat = new DecimalFormat("##.##");
        this.xx = Double.parseDouble(decimalFormat.format(this.xx));
        this.yy = Double.parseDouble(decimalFormat.format(this.yy));
        this.zz = Double.parseDouble(decimalFormat.format(this.zz));

        return this;
    }

}
