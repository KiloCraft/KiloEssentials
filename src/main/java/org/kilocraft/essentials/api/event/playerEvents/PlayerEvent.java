package org.kilocraft.essentials.api.event.playerEvents;

import net.minecraft.entity.player.PlayerEntity;
import org.kilocraft.essentials.api.event.Event;

public interface PlayerEvent extends Event {

    /**
     * Returns the player that fired this event
     *
     * @return the player that fired this event
     */
    PlayerEntity getPlayer();

}
