package org.kilocraft.essentials.api.world.location;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.util.EntityRotation;
import org.kilocraft.essentials.util.LocationUtil;
import org.kilocraft.essentials.util.player.PlayerRotation;
import org.kilocraft.essentials.util.registry.RegistryUtils;

public class Vec3iLocation implements Location {
    private int x, y, z;
    private EntityRotation rotation;
    private ResourceLocation dimension;

    private Vec3iLocation(int x, int y, int z, float yaw, float pitch, ResourceLocation dimension) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.rotation = new PlayerRotation(yaw, pitch);
        this.dimension = dimension;
    }

    public static Vec3iLocation of(int x, int y, int z, float yaw, float pitch, ResourceLocation dimension) {
        return new Vec3iLocation(x, y, z, yaw, pitch, dimension);
    }

    public static Vec3iLocation of(int x, int y, int z, float yaw, float pitch) {
        return new Vec3iLocation(x, y, z, yaw, pitch, null);
    }

    public static Vec3iLocation of(int x, int y, int z) {
        return new Vec3iLocation(x, y, z, 0.0F, 0.0F, null);
    }

    public static Vec3iLocation of(Vec3i Vec3i) {
        return of(Vec3i.getX(), Vec3i.getY(), Vec3i.getZ());
    }

    public static Vec3iLocation of(ServerPlayer player) {
        return new Vec3iLocation((int) player.getX(), (int) player.getY(), (int) player.getZ(), player.getYRot(), player.getXRot(), RegistryUtils.toIdentifier(player.getLevel().dimensionType()));
    }

    public static Vec3iLocation of(OnlineUser user) {
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

    @Override
    public ResourceLocation getDimension() {
        return this.dimension;
    }

    @Override
    public DimensionType getDimensionType() {
        return RegistryUtils.toDimension(this.dimension);
    }

    @Override
    public EntityRotation getRotation() {
        return this.rotation;
    }

    @Override
    public ServerLevel getWorld() {
        return RegistryUtils.toServerWorld(this.getDimensionType());
    }

    @Override
    public boolean isSafe() {
        return LocationUtil.isBlockSafe(this);
    }

    @Override
    public boolean isSafeFor(OnlineUser user) {
        return LocationUtil.isBlockSafeFor(user, this);
    }

    @Override
    public boolean isSafeFor(ServerPlayer player) {
        return this.isSafeFor(KiloEssentials.getUserManager().getOnline(player));
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        CompoundTag pos = new CompoundTag();

        pos.putInt("x", this.x);
        pos.putInt("y", this.y);
        pos.putInt("z", this.z);

        tag.put("pos", pos);

        if (this.dimension != null)
            tag.putString("dim", this.dimension.toString());

        if (this.rotation.getYaw() != 0 && this.rotation.getPitch() != 0) {
            CompoundTag view = new CompoundTag();
            view.putFloat("yaw", this.rotation.getYaw());
            view.putFloat("pitch", this.rotation.getPitch());
            tag.put("view", view);
        }

        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        CompoundTag pos = tag.getCompound("pos");

        this.x = pos.getInt("x");
        this.y = pos.getInt("y");
        this.z = pos.getInt("z");

        if (tag.contains("dim"))
            this.dimension = new ResourceLocation(tag.getString("dim"));

        if (tag.contains("view")) {
            CompoundTag view = tag.getCompound("view");
            this.rotation = new PlayerRotation(view.getFloat("yaw"), view.getFloat("pitch"));
        }
    }

    @Override
    public void setX(double x) {
        this.x = (int) x;
    }

    @Override
    public void setY(double y) {
        this.y = (int) y;
    }

    @Override
    public void setZ(double z) {
        this.z = (int) z;
    }

    @Override
    public void setRotation(float yaw, float pitch) {
        this.rotation = new PlayerRotation(yaw, pitch);
    }

    @Override
    public void setDimension(ResourceLocation dimension) {
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
    public Vec3 toVec3d() {
        return new Vec3(this.x, this.y, this.z);
    }

    @Override
    public Vec3i toVec3i() {
        return new Vec3i(this.z, this.y, this.z);
    }

    public Vec3dLocation toVec3dLocation() {
        return Vec3dLocation.of(this.x, this.y, this.z, this.rotation.getYaw(), this.rotation.getPitch(), this.dimension);
    }

    public static Vec3iLocation dummy() {
        return of(0, 100, 0);
    }
}
