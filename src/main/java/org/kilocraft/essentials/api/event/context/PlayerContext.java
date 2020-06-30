package org.kilocraft.essentials.api.event.context;

import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.user.OnlineUser;

/**
 * Represents a context where a player is involved, often as the cause of the event.
 */
public interface PlayerContext extends Contextual {
    /**
     * Returns the player that fired this event
     * @return the player that fired this event
     */
    ServerPlayerEntity getPlayer();

    /**
     * Gets the User that fired this event
     * @return the users that fired this event
     */
    OnlineUser getUser();
}
