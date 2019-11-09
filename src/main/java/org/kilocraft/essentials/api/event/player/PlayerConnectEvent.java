package org.kilocraft.essentials.api.event.player;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.kilocraft.essentials.api.event.Event;
import org.kilocraft.essentials.api.event.context.CancellableReasonContext;
import org.kilocraft.essentials.api.event.context.PlayerContext;
import org.kilocraft.essentials.api.event.context.ServerContext;

import java.net.InetAddress;

public interface PlayerConnectEvent extends Event, PlayerContext, ServerContext, CancellableReasonContext {
    /**
     * Gets the address of this connection
     * @return the address of this connection
     */
    InetAddress getAddress();

    ClientConnection getClientConnection();

    ServerPlayNetworkHandler getNetworkHandler();
}
