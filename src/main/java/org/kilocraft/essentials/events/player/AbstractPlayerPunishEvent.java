package org.kilocraft.essentials.events.player;

import org.kilocraft.essentials.api.event.player.PlayerPunishEventInterface;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.util.EntityIdentifiable;

public abstract class AbstractPlayerPunishEvent implements PlayerPunishEventInterface {

    private final CommandSourceUser source;
    private final EntityIdentifiable victim;
    private final String reason;
    private final long expiry;
    private final boolean silent;

    public AbstractPlayerPunishEvent(CommandSourceUser source, EntityIdentifiable victim, String reason, long expiry, boolean silent) {
        this.source = source;
        this.victim = victim;
        this.reason = reason;
        this.expiry = expiry;
        this.silent = silent;
    }

    @Override
    public CommandSourceUser getSource() {
        return this.source;
    }

    @Override
    public EntityIdentifiable getVictim() {
        return this.victim;
    }

    @Override
    public String getReason() {
        return this.reason;
    }

    @Override
    public long getExpiry() {
        return this.expiry;
    }

    @Override
    public boolean isSilent() {
        return this.silent;
    }

    @Override
    public boolean isPermanent() {
        return this.expiry == -1;
    }
}
