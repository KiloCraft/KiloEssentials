package org.kilocraft.essentials.api.event.eventImpl.playerEventsImpl;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.event.playerEvents.PlayerEvent$OnConnect;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class PlayerEvent$OnConnectImpl implements PlayerEvent$OnConnect {

    private String cancelReason = "Disconnected";

    private ClientConnection connection;
    private ServerPlayerEntity player;
    private ServerPlayNetworkHandler networkHandler;
    private boolean isCancelled = false;

    public PlayerEvent$OnConnectImpl(ClientConnection connection, ServerPlayerEntity playerEntity) {
        this.connection = connection;
        this.player = playerEntity;
        this.networkHandler = networkHandler;
    }

    public ServerPlayerEntity getPlayer() {
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

    @Override
    public ClientConnection getClientConnection() {
        return this.connection;
    }

    @Override
    public ServerPlayNetworkHandler getNetworkHandler() {
        return this.networkHandler;
    }

    @Override
    public MinecraftServer getServer() {
        return player.getServer();
    }
}
