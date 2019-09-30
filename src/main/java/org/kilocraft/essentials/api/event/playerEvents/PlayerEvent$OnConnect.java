package org.kilocraft.essentials.api.event.playerEvents;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.kilocraft.essentials.api.event.CancellableWithReason;
import org.kilocraft.essentials.api.event.serverEvents.ServerEvent;

import java.net.InetAddress;

public interface PlayerEvent$OnConnect extends PlayerEvent, CancellableWithReason, ServerEvent {

    /**
     * Gets the address of this connection
     * @return the address of this connection
     */
    InetAddress getAddress();

    ClientConnection getClientConnection();

    ServerPlayNetworkHandler getNetworkHandler();

}