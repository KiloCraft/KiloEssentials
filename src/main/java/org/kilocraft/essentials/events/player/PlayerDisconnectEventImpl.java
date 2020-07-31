package org.kilocraft.essentials.events.player;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.event.player.PlayerDisconnectEvent;
import org.kilocraft.essentials.api.user.OnlineUser;

public class PlayerDisconnectEventImpl implements PlayerDisconnectEvent {

    private final ServerPlayerEntity player;
    private final OnlineUser user;

    public PlayerDisconnectEventImpl(ServerPlayerEntity playerEntity, OnlineUser user) {
        this.player = playerEntity;
        this.user = user;
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
        return this.user;
    }
}

