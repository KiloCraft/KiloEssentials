package org.kilocraft.essentials.extensions.homes.api;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;
import org.kilocraft.essentials.api.user.OnlineUser;

import java.util.UUID;

public class Home {
    private UUID owner_uuid;
    private String name;
    private Identifier dimensionId;
    private double x, y, z;
    private float dX, dY;

    public Home(UUID uuid, String name, double x, double y, double z, Identifier dimensionId, float yaw, float pitch) {
        this.owner_uuid = uuid;
        this.name = name;
        this.dimensionId = dimensionId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.dY = yaw;
        this.dX = pitch;
    }

    public Home() {
    }

    public Home(CompoundTag compoundTag) {
        fromTag(compoundTag);
    }

    public CompoundTag toTag() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("dimension", this.dimensionId.toString());

        CompoundTag pos = new CompoundTag();
        pos.putDouble("x", this.x);
        pos.putDouble("y", this.y);
        pos.putDouble("z", this.z);

        compoundTag.put("pos", pos);

        CompoundTag dir = new CompoundTag();
        dir.putDouble("dX", dX);
        dir.putDouble("dY", dY);

        compoundTag.put("dir", dir);
        return compoundTag;
    }

    public void fromTag(CompoundTag compoundTag) {
        this.dimensionId = new Identifier(compoundTag.getString("dimension"));

        CompoundTag pos = compoundTag.getCompound("pos");
        this.x = pos.getDouble("x");
        this.y = pos.getDouble("y");
        this.z = pos.getDouble("z");

        CompoundTag dir = compoundTag.getCompound("dir");
        this.dX = dir.getFloat("dX");
        this.dY = dir.getFloat("dY");
    }

    public UUID getOwner() {
        return owner_uuid;
    }

    public void setOwner(UUID uuid) {
        this.owner_uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public Identifier getDimId() {
        return dimensionId;
    }

    public void setDimension(Identifier dimensionType) {
        this.dimensionId = dimensionId;
    }

    public float getPitch() {
        return dX;
    }

    public void setPitch(float dX) {
        this.dX = dX;
    }

    public float getYaw() {
        return dY;
    }

    public void setYaw(float dY) {
        this.dY = dY;
    }

    public static void teleportTo(OnlineUser user, Home home) throws CommandSyntaxException {
        ServerPlayerEntity player = user.getPlayer();
        DimensionType type = DimensionType.byId(home.getDimId());
        if(type == null) {
            return;
        }

        ServerWorld destinationWorld = player.getServer().getWorld(type);
        Vec3d destination = new Vec3d(home.getX(), home.getY(), home.getZ());
        float yaw = home.getYaw();
        float pitch = home.getPitch();

        destinationWorld.getChunkManager().addTicket(ChunkTicketType.POST_TELEPORT, new ChunkPos(new BlockPos(destination)), 1, player.getEntityId()); // Lag reduction magic
        player.teleport(destinationWorld, home.getX(), home.getY(), home.getZ(), home.getYaw(), home.getPitch());
    }
}
