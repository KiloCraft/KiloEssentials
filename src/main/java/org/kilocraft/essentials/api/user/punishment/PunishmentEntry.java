package org.kilocraft.essentials.api.user.punishment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.util.EntityIdentifiable;

public abstract class PunishmentEntry {
    private @Nullable
    final EntityIdentifiable victim;
    private final EntityIdentifiable arbiter;
    private @Nullable
    final String reason;
    private @Nullable
    final String victimIP;

    public PunishmentEntry(@NotNull final EntityIdentifiable arbiter, @Nullable final EntityIdentifiable victim, @Nullable final String victimIP, @Nullable final String reason) {
        this.arbiter = arbiter;
        this.victim = victim;
        this.reason = reason;
        this.victimIP = victimIP;
    }

    public EntityIdentifiable getArbiter() {
        return this.arbiter;
    }

    @Nullable
    public EntityIdentifiable getVictim() {
        return this.victim;
    }

    @Nullable
    public String getVictimIP() {
        return this.victimIP;
    }

    @Nullable
    public String getReason() {
        return this.reason;
    }
}
