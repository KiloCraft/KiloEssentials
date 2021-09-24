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
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.util.EntityRotation;
import org.kilocraft.essentials.util.registry.RegistryUtils;

public interface Location {
    double getX();

    double getY();

    double getZ();

    Identifier getDimension();

    DimensionType getDimensionType();

    EntityRotation getRotation();

    ServerWorld getWorld();

    boolean isSafe();

    boolean isSafeFor(OnlineUser user);

    boolean isSafeFor(ServerPlayerEntity player);

    NbtCompound toTag();

    void fromTag(NbtCompound tag);

    void setX(double x);

    void setY(double y);

    void setZ(double z);

    void setRotation(float yaw, float pitch);

    void setDimension(Identifier dimension);

    void setDimension(DimensionType type);

    ChunkPos toChunkPos();

    BlockPos toPos();

    Vec3d toVec3d();

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
            builder.append(RegistryUtils.dimensionToName(this.getWorld().getDimension()));
        }

        builder.append("&8/&e").append(Math.round(this.getX())).append(", ").append(Math.round(this.getY())).append(", ").append(Math.round(this.getZ()));
        return builder.toString();
    }

}
