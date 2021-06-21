package org.kilocraft.essentials.events.player;

import org.kilocraft.essentials.api.event.player.PlayerMutedEvent;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.util.EntityIdentifiable;

public class PlayerMutedEventImpl extends AbstractPlayerPunishEvent implements PlayerMutedEvent {

    public PlayerMutedEventImpl(CommandSourceUser source, EntityIdentifiable victim, String reason, long expiry, boolean silent) {
        super(source, victim, reason, expiry, silent);
    }

}
