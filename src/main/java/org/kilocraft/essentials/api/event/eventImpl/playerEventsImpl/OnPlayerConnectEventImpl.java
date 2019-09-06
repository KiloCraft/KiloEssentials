package org.kilocraft.essentials.api.event.eventImpl.playerEventsImpl;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.ClientConnection;
import org.kilocraft.essentials.api.event.playerEvents.OnPlayerConnectEvent;

import net.minecraft.server.network.ServerPlayerEntity;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class OnPlayerConnectEventImpl implements org.kilocraft.essentials.api.event.playerEvents.OnPlayerConnectEvent {

    private String cancelReason = "Disconnected";

    private ClientConnection connection;
    private PlayerEntity player;
    private boolean isCancelled = false;

    public OnPlayerConnectEventImpl(ClientConnection connection, ServerPlayerEntity playerEntity) {
        this.connection = connection;
        this.player = playerEntity;
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    @Override
    public String getCancelReason() {
        return cancelReason;
    }

    @Override
    public void setCancelReason(String reason) {
        cancelReason = reason;
    }

    @Override
    public InetAddress getAddress() {
        return ((InetSocketAddress) connection.getAddress()).getAddress();
    }

}
