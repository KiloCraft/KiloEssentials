package org.kilocraft.essentials.api.world.location;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.dimension.DimensionType;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.util.EntityRotation;
import org.kilocraft.essentials.util.LocationUtil;
import org.kilocraft.essentials.util.player.PlayerRotation;
import org.kilocraft.essentials.util.registry.RegistryUtils;

public class Vec3iLocation implements Location {
    private int x, y, z;
    private EntityRotation rotation;
    private Identifier dimension;

    private Vec3iLocation(int x, int y, int z, float yaw, float pitch, Identifier dimension) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.rotation = new PlayerRotation(yaw, pitch);
        this.dimension = dimension;
    }

    public static Vec3iLocation of(int x, int y, int z, float yaw, float pitch, Identifier dimension) {
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

    public static Vec3iLocation of(ServerPlayerEntity player) {
        return new Vec3iLocation((int) player.getX(), (int) player.getY(), (int) player.getZ(), player.getYaw(), player.getPitch(), RegistryUtils.toIdentifier(player.getServerWorld().getDimension()));
    }

    public static Vec3iLocation of(OnlineUser user) {
        return of(user.asPlayer());
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public double getZ() {
        return z;
    }

    @Override
    public Identifier getDimension() {
        return dimension;
    }

    @Override
    public DimensionType getDimensionType() {
        return RegistryUtils.toDimension(this.dimension);
    }

    @Override
    public EntityRotation getRotation() {
        return rotation;
    }

    @Override
    public ServerWorld getWorld() {
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
    public boolean isSafeFor(ServerPlayerEntity player) {
        return isSafeFor(KiloEssentials.getUserManager().getOnline(player));
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        NbtCompound pos = new NbtCompound();

        pos.putInt("x", this.x);
        pos.putInt("y", this.y);
        pos.putInt("z", this.z);

        tag.put("pos", pos);

        if (this.dimension != null)
            tag.putString("dim", this.dimension.toString());

        if (rotation.getYaw() != 0 && rotation.getPitch() != 0) {
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

        this.x = pos.getInt("x");
        this.y = pos.getInt("y");
        this.z = pos.getInt("z");

        if (tag.contains("dim"))
            this.dimension = new Identifier(tag.getString("dim"));

        if (tag.contains("view")) {
            NbtCompound view = tag.getCompound("view");
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
    public void setDimension(Identifier dimension) {
        this.dimension = dimension;
    }

    @Override
    public void setDimension(DimensionType type) {
        this.dimension = RegistryUtils.toIdentifier(type);
    }

    @Override
    public ChunkPos toChunkPos() {
        return new ChunkPos(toPos());
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

    @Override
    public Location up() {
        this.y += 1;
        return this;
    }

    @Override
    public Location down() {
        this.y -= 1;
        return this;
    }

    public Vec3dLocation toVec3dLocation() {
        return Vec3dLocation.of(x, y, z, rotation.getYaw(), rotation.getPitch(), dimension);
    }

    public static Vec3iLocation dummy() {
        return of(0, 100, 0);
    }
}
