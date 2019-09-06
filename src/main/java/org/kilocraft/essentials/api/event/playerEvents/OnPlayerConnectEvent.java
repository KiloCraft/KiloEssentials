package org.kilocraft.essentials.api.event.playerEvents;

import org.kilocraft.essentials.api.event.CancellableWithReason;

import java.net.InetAddress;

public interface OnPlayerConnectEvent extends PlayerEvent, CancellableWithReason {

    /**
     * Gets the address of this connection
     * @return the address of this connection
     */
    InetAddress getAddress();

}