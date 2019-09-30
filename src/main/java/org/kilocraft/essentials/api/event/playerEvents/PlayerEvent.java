package org.kilocraft.essentials.api.event.playerEvents;

import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.event.Event;

public interface PlayerEvent extends Event {

    /**
     * Returns the player that fired this event
     *
     * @return the player that fired this event
     */
    ServerPlayerEntity getPlayer();

}
