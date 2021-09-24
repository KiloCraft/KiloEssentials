package org.kilocraft.essentials.api.world.location;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.util.EntityRotation;
import org.kilocraft.essentials.util.LocationUtil;
import org.kilocraft.essentials.util.player.PlayerRotation;
import org.kilocraft.essentials.util.registry.RegistryUtils;

import java.text.DecimalFormat;
import java.util.Objects;

public class Vec3dLocation implements Location {
    private static final DecimalFormat decimalFormat = new DecimalFormat("##.##");
    private double x, y, z;
    private EntityRotation rotation;
    private Identifier dimension;
    private boolean useShortDecimals = false;

    private Vec3dLocation(double x, double y, double z, float yaw, float pitch, Identifier dimension) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.rotation = new PlayerRotation(yaw, pitch);
        this.dimension = dimension;
    }

    public static Vec3dLocation of(double x, double y, double z, float yaw, float pitch, Identifier dimension) {
        return new Vec3dLocation(x, y, z, yaw, pitch, dimension);
    }

    public static Vec3dLocation of(double x, double y, double z, float yaw, float pitch) {
        return new Vec3dLocation(x, y, z, yaw, pitch, null);
    }

    public static Vec3dLocation of(double x, double y, double z) {
        return new Vec3dLocation(x, y, z, 0.0F, 0.0F, null);
    }

    public static Vec3dLocation of(Vec3d vec3d) {
        return of(vec3d.getX(), vec3d.getY(), vec3d.getZ());
    }

    public static Vec3dLocation of(ServerPlayerEntity player) {
        Identifier dim = null;
        if (player.getWorld() != null && player.getWorld().getDimension() != null) {
            dim = RegistryUtils.toIdentifier(player.getWorld().getDimension());
        }
        return new Vec3dLocation(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch(), dim);
    }

    public static Vec3dLocation of(Entity entity) {
        Identifier dim = null;
        if (entity.getEntityWorld() != null && entity.getEntityWorld().getDimension() != null) {
            dim = RegistryUtils.toIdentifier(entity.getEntityWorld().getDimension());
        }
        return new Vec3dLocation(entity.getX(), entity.getY(), entity.getZ(), entity.getYaw(), entity.getPitch(), dim);
    }

    public static Vec3dLocation of(OnlineUser user) {
        return of(user.asPlayer());
    }

    @Override
    public double getX() {
        return this.x;
    }

    @Override
    public double getY() {
        return this.y;
    }

    @Override
    public double getZ() {
        return this.z;
    }

    @Nullable
    @Override
    public Identifier getDimension() {
        return this.dimension;
    }

    @Nullable
    @Override
    public DimensionType getDimensionType() {
        return RegistryUtils.toDimension(this.dimension);
    }

    @Override
    public EntityRotation getRotation() {
        return this.rotation;
    }

    @Nullable
    @Override
    public ServerWorld getWorld() {
        return this.dimension == null ? null : RegistryUtils.toServerWorld(Objects.requireNonNull(this.getDimensionType(), "Null dimension provided"));
    }

    @Override
    public boolean isSafe() {
        return false;
    }

    @Override
    public boolean isSafeFor(OnlineUser user) {
        return LocationUtil.isBlockSafeFor(user, this);
    }

    @Override
    public boolean isSafeFor(ServerPlayerEntity player) {
        return this.isSafeFor(KiloEssentials.getUserManager().getOnline(player));
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        NbtCompound pos = new NbtCompound();

        if (this.useShortDecimals)
            this.shortDecimals();

        pos.putDouble("x", this.x);
        pos.putDouble("y", this.y);
        pos.putDouble("z", this.z);

        tag.put("pos", pos);

        if (this.dimension != null)
            tag.putString("dim", this.dimension.toString());

        if (this.rotation.getYaw() != 0 && this.rotation.getPitch() != 0) {
            NbtCompound view = new NbtCompound();
            view.putFloat("yaw", this.rotation.getYaw());
            view.putFloat("pitch", this.rotation.getPitch());

            tag.put("view", view);
        }

        return tag;
    }

    @Override
    public void fromTag(NbtCompound tag) {
        NbtCompound pos = tag.getCompound("pos");

        this.x = pos.getDouble("x");
        this.y = pos.getDouble("y");
        this.z = pos.getDouble("z");

        if (tag.contains("dim"))
            this.dimension = new Identifier(tag.getString("dim"));

        if (tag.contains("view")) {
            NbtCompound view = tag.getCompound("view");
            this.rotation = new PlayerRotation(view.getFloat("yaw"), view.getFloat("pitch"));
        }
    }

    @Override
    public void setX(double x) {
        this.x = x;
    }

    @Override
    public void setY(double y) {
        this.y = y;
    }

    @Override
    public void setZ(double z) {
        this.z = z;
    }

    @Override
    public void setRotation(float yaw, float pitch) {
        this.rotation = new PlayerRotation(yaw, pitch);
    }

    @Override
    public void setDimension(Identifier dimension) {
        this.dimension = dimension;
    }

    @Override
    public void setDimension(DimensionType type) {
        this.dimension = RegistryUtils.toIdentifier(type);
    }

    @Override
    public ChunkPos toChunkPos() {
        return new ChunkPos(this.toPos());
    }

    @Override
    public BlockPos toPos() {
        return new BlockPos(this.x, this.y, this.z);
    }

    @Override
    public Vec3d toVec3d() {
        return new Vec3d(this.x, this.y, this.z);
    }

    @Override
    public Vec3i toVec3i() {
        return new Vec3i(this.z, this.y, this.z);
    }

    public Vec3iLocation toVec3iLocation() {
        return Vec3iLocation.of((int) this.x, (int) this.y, (int) this.z, this.rotation.getYaw(), this.rotation.getPitch(), this.dimension);
    }

    public static Vec3dLocation dummy() {
        return of(0, 100, 0);
    }

    public void setVector(Vec3d vector) {
        this.x = vector.getX();
        this.y = vector.getY();
        this.z = vector.getZ();
    }

    public Vec3dLocation shortDecimals() {
        this.useShortDecimals = true;

        this.x = Double.parseDouble(decimalFormat.format(this.x));
        this.y = Double.parseDouble(decimalFormat.format(this.y));
        this.z = Double.parseDouble(decimalFormat.format(this.z));

        this.setRotation(Float.parseFloat(decimalFormat.format(this.rotation.getYaw())),
                Float.parseFloat(decimalFormat.format(this.rotation.getPitch())));

        return this;
    }

    public Vec3dLocation center() {
        return of(this.x + 0.5D, this.y, this.z + 0.5D, this.rotation.getYaw(), this.rotation.getPitch(), this.dimension);
    }

    @Override
    public String toString() {
        return "x: " + this.x + " y: " + this.y + " z: " + this.z;
    }

    public boolean isUsingShortDecimals() {
        return this.useShortDecimals;
    }
}
