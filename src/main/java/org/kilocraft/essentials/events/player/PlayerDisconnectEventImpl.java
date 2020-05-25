package org.kilocraft.essentials.events.player;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.player.PlayerDisconnectEvent;
import org.kilocraft.essentials.api.user.OnlineUser;

public class PlayerDisconnectEventImpl implements PlayerDisconnectEvent {

    private ServerPlayerEntity player;

    public PlayerDisconnectEventImpl(ServerPlayerEntity playerEntity) {
        this.player = playerEntity;
    }

    @Override
    public ServerPlayerEntity getPlayer() {
        return player;
    }

    @Override
    public MinecraftServer getServer() {
        return player.getServer();
    }

    @Override
    public OnlineUser getUser() {
        return KiloServer.getServer().getOnlineUser(this.player);
    }
}

