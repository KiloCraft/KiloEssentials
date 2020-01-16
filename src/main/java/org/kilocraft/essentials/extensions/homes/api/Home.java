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
import org.kilocraft.essentials.util.Location;

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
            this.location = Location.dummy();

        if (compoundTag.contains("pos")) { //OLD Format
            this.location.setDimension(new Identifier(compoundTag.getString("dimension")));

            CompoundTag pos = compoundTag.getCompound("pos");
            this.location.setPos(
                    new BlockPos(pos.getDouble("x"), pos.getDouble("y"), pos.getDouble("z")));

            CompoundTag dir = compoundTag.getCompound("dir");
            this.location.setView(dir.getFloat("dX"), dir.getFloat("dY"));
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

    public static void teleportTo(OnlineUser user, Home home) throws CommandSyntaxException {
        ServerPlayerEntity player = user.getPlayer();
        DimensionType type = DimensionType.byId(home.getLocation().getDimensionId());
        if (type == null)
            return;

        ServerWorld destinationWorld = player.getServer().getWorld(type);
        Vec3d destination = new Vec3d(home.getLocation().getX(), home.getLocation().getY(), home.getLocation().getZ());
        float yaw = home.getLocation().getYaw();
        float pitch = home.getLocation().getPitch();

        destinationWorld.getChunkManager().addTicket(ChunkTicketType.POST_TELEPORT, new ChunkPos(new BlockPos(destination)), 1, player.getEntityId()); // Lag reduction magic
        player.teleport(destinationWorld, home.getLocation().getX(), home.getLocation().getY(), home.getLocation().getZ(),
                home.getLocation().getYaw(), home.getLocation().getPitch());
    }
}
