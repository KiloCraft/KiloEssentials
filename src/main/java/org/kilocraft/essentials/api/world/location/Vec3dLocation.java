package org.kilocraft.essentials.api.world.location;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.util.EntityRotation;
import org.kilocraft.essentials.util.PlayerRotation;
import org.kilocraft.essentials.util.RegistryUtils;

public class Vec3dLocation implements Location {
    private double x, y, z;
    private EntityRotation rotation;
    private Identifier dimension;

    private Vec3dLocation(double x, double y, double z, float yaw, float pitch, Identifier dimension) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.rotation = new PlayerRotation(yaw, pitch);
        this.dimension = dimension;
    }

    public Vec3dLocation of(double x, double y, double z, float yaw, float pitch, Identifier dimension) {
        return new Vec3dLocation(x, y, z, yaw, pitch, dimension);
    }

    public Vec3dLocation of(double x, double y, double z, float yaw, float pitch) {
        return new Vec3dLocation(x, y, z, yaw, pitch, null);
    }

    public Vec3dLocation of(double x, double y, double z) {
        return new Vec3dLocation(x, y, z, 0.0F, 0.0F, null);
    }

    public Vec3dLocation of(Vec3d vec3d) {
        return of(vec3d.getX(), vec3d.getY(), vec3d.getZ());
    }

    public Vec3dLocation of(ServerPlayerEntity player) {
        return new Vec3dLocation(player.getX(), player.getY(), player.getZ(), player.yaw, player.pitch, RegistryUtils.toIdentifier(player.dimension));
    }

    public Vec3dLocation of(OnlineUser user) {
        return of(user.getPlayer());
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
    public EntityRotation getRotation() {
        return rotation;
    }

    @Override
    public ServerWorld getWorld() {
        return KiloServer.getServer().getVanillaServer().getWorld(RegistryUtils.toDimension(dimension));
    }

    @Override
    public boolean isSafe() {
        return false;
    }

    @Override
    public boolean isSafeFor(OnlineUser user) {
        return false;
    }

    @Override
    public boolean isSafeFor(ServerPlayerEntity player) {
        return false;
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        CompoundTag pos = new CompoundTag();

        pos.putDouble("x", this.x);
        pos.putDouble("y", this.y);
        pos.putDouble("z", this.z);

        tag.put("pos", pos);

        if (this.dimension != null)
            tag.putString("dim", this.dimension.toString());

        if (rotation.getYaw() != 0 && rotation.getPitch() != 0) {
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

        this.x = pos.getDouble("x");
        this.y = pos.getDouble("y");
        this.z = pos.getDouble("z");

        if (tag.contains("dim"))
            this.dimension = new Identifier(tag.getString("dim"));

        if (tag.contains("view")) {
            CompoundTag view = tag.getCompound("view");
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
    public void setDimension(Identifier dimension) {
        this.dimension = dimension;
    }

    @Override
    public void setDimension(DimensionType type) {
        this.dimension = RegistryUtils.toIdentifier(type);
    }

}
