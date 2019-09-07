package org.kilocraft.essentials.api.event.playerEvents;

import org.kilocraft.essentials.api.event.CancellableWithReason;
import org.kilocraft.essentials.api.event.MessagedEvent;

import java.net.InetAddress;

public interface PlayerEvent$OnConnect extends PlayerEvent, CancellableWithReason {

    /**
     * Gets the address of this connection
     * @return the address of this connection
     */
    InetAddress getAddress();

}