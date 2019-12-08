package org.kilocraft.essentials.user;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.user.OnlineUser;
import java.util.UUID;

public class OnlineServerUser extends ServerUser implements OnlineUser {

    public ServerPlayerEntity getPlayer() {
        return KiloServer.getServer().getPlayer(this.uuid);
    }

    public ServerCommandSource getCommandSource() {
        return this.getPlayer().getCommandSource();
    }

    public static OnlineServerUser of(UUID uuid) {
        return (OnlineServerUser) manager.getOnline(uuid);
    }

    public static OnlineServerUser of(String name) {
        return (OnlineServerUser) manager.getOnline(name);
    }

    public static OnlineServerUser of(GameProfile profile) {
        return of(profile.getId());
    }

    public static OnlineServerUser of(ServerPlayerEntity player) {
        return of(player.getUuid());
    }

    public OnlineServerUser(ServerPlayerEntity player) {
        super(player.getUuid());
        this.name = player.getEntityName();
    }

    @Override
    protected void deserialize(CompoundTag tag) {
        // All the other serialization logic is handled.
        super.deserialize(tag);
    }
}
