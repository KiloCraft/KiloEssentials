package org.kilocraft.essentials.api.world.location;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionType;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.util.EntityRotation;

public interface Location {
    double getX();
    double getY();
    double getZ();

    Identifier getDimension();

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

    void setDimension(Identifier dimension);
    void setDimension(DimensionType type);
}
