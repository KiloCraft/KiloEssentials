package org.kilocraft.essentials.api.world.location;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.dimension.DimensionType;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.util.EntityRotation;

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

    CompoundTag toTag();
    void fromTag(CompoundTag tag);

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

}
