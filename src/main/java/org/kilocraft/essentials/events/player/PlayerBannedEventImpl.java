package org.kilocraft.essentials.events.player;

import org.kilocraft.essentials.api.event.player.PlayerBannedEvent;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.util.EntityIdentifiable;

public class PlayerBannedEventImpl extends AbstractPlayerPunishEvent implements PlayerBannedEvent {

    private final boolean ip;

    public PlayerBannedEventImpl(CommandSourceUser source, EntityIdentifiable victim, String reason, long expiry, boolean silent, boolean ipBan) {
        super(source, victim, reason, expiry, silent);
        this.ip = ipBan;
    }

    @Override
    public boolean isIpBan() {
        return this.ip;
    }
}
