package org.kilocraft.essentials.api.event.eventImpl.playerEventsImpl;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.event.playerEvents.PlayerEvent$OnDisconnect;

public class PlayerEvent$OnDisconnectImpl implements PlayerEvent$OnDisconnect {

    private ServerPlayerEntity player;

    public PlayerEvent$OnDisconnectImpl(ServerPlayerEntity playerEntity) {
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

