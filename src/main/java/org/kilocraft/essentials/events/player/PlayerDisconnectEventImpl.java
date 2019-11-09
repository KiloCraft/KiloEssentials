package org.kilocraft.essentials.events.player;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.event.player.PlayerDisconnectEvent;

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
}

