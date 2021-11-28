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
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.util.EntityRotation;
import org.kilocraft.essentials.util.registry.RegistryUtils;

public interface Location {
    double getX();

    double getY();

    double getZ();

    ResourceLocation getDimension();

    DimensionType getDimensionType();

    EntityRotation getRotation();

    ServerLevel getWorld();

    boolean isSafe();

    boolean isSafeFor(OnlineUser user);

    boolean isSafeFor(ServerPlayer player);

    CompoundTag toTag();

    void fromTag(CompoundTag tag);

    void setX(double x);

    void setY(double y);

    void setZ(double z);

    void setRotation(float yaw, float pitch);

    void setDimension(ResourceLocation dimension);

    void setDimension(DimensionType type);

    ChunkPos toChunkPos();

    BlockPos toPos();

    Vec3 toVec3d();

    Vec3i toVec3i();

    default double squaredDistanceTo(Location location) {
        double d = location.getX() - this.getX();
        double e = location.getY() - this.getY();
        double f = location.getZ() - this.getZ();
        return d * d + e * e + f * f;
    }

    default Location up() {
        this.setY(this.getY() + 1);
        return this;
    }

    default Location down() {
        this.setY(this.getY() - 1);
        return this;
    }
    default String asString() {
        return ComponentText.clearFormatting(this.asFormattedString());
    }

    default String asFormattedString() {
        StringBuilder builder = new StringBuilder("&d");

        if (this.getWorld() != null) {
            builder.append(RegistryUtils.dimensionToName(this.getWorld().dimensionType()));
        }

        builder.append("&8/&e").append(Math.round(this.getX())).append(", ").append(Math.round(this.getY())).append(", ").append(Math.round(this.getZ()));
        return builder.toString();
    }

}
