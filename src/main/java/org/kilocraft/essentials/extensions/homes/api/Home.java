package org.kilocraft.essentials.extensions.homes.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.world.location.Location;
import org.kilocraft.essentials.api.world.location.Vec3dLocation;
import org.kilocraft.essentials.util.LocationUtil;

import java.util.UUID;

public class Home {
    private UUID owner_uuid;
    private String name;
    private Location location;

    public Home(UUID uuid, String name, Location location) {
        this.owner_uuid = uuid;
        this.name = name;
        this.location = location;
    }

    public Home() {
    }

    public Home(CompoundTag compoundTag) {
        fromTag(compoundTag);
    }

    public CompoundTag toTag() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.put("loc", this.location.toTag());

        return compoundTag;
    }

    public void fromTag(CompoundTag compoundTag) {
        if (this.location == null)
            this.location = Vec3dLocation.dummy();

        if (compoundTag.contains("pos")) { //OLD Format
            this.location.setDimension(new Identifier(compoundTag.getString("dimension")));

            CompoundTag pos = compoundTag.getCompound("pos");
            ((Vec3dLocation) this.location).setVector(new Vec3d(pos.getDouble("x"), pos.getDouble("y"), pos.getDouble("z")));

            CompoundTag dir = compoundTag.getCompound("dir");
            this.location.setRotation(dir.getFloat("dY"), dir.getFloat("dX"));
            return;
        }

        this.location.fromTag(compoundTag.getCompound("loc"));
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

    public Location getLocation() {
        return this.location;
    }

    public static void teleportTo(OnlineUser user, Home home) {
        ServerPlayerEntity player = user.asPlayer();
        DimensionType type = DimensionType.byId(home.getLocation().getDimension());
        if (type == null)
            return;

        ServerWorld destinationWorld = KiloServer.getServer().getVanillaServer().getWorld(type);
        Vec3d destination = new Vec3d(home.getLocation().getX(), home.getLocation().getY(), home.getLocation().getZ());
        user.saveLocation();
        destinationWorld.getChunkManager().addTicket(ChunkTicketType.POST_TELEPORT, new ChunkPos(new BlockPos(destination)), 1, player.getEntityId()); // Lag reduction magic
        player.teleport(destinationWorld, home.getLocation().getX(), home.getLocation().getY(), home.getLocation().getZ(),
                home.getLocation().getRotation().getYaw(), home.getLocation().getRotation().getPitch());
    }

    public boolean shouldTeleport() {
        return !LocationUtil.isDimensionValid(this.location.getDimensionType());
    }

}
