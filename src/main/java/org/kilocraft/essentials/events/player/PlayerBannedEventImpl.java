package org.kilocraft.essentials.events.player;

import org.kilocraft.essentials.api.event.player.PlayerMutedEvent;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.util.EntityIdentifiable;

public class PlayerBannedEventImpl implements PlayerMutedEvent {

    private final EntityIdentifiable victim;
    private final CommandSourceUser source;
    private final String reason;

    public PlayerBannedEventImpl(EntityIdentifiable victim, CommandSourceUser source, String reason) {
        this.victim = victim;
        this.source = source;
        this.reason = reason;
    }

    @Override
    public CommandSourceUser getSource() {
        return this.source;
    }

    @Override
    public String getReason() {
        return this.reason;
    }

    @Override
    public EntityIdentifiable getVictim() {
        return this.victim;
    }
}
