package org.kilocraft.essentials.api.user.punishment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.util.EntityIdentifiable;

public abstract class PunishmentEntry {
    private final EntityIdentifiable arbiter, victim;
    private @Nullable
    final String reason;

    public PunishmentEntry(@NotNull final EntityIdentifiable arbiter, @NotNull final EntityIdentifiable victim, @Nullable final String reason) {
        this.arbiter = arbiter;
        this.victim = victim;
        this.reason = reason;
    }

    public EntityIdentifiable getArbiter() {
        return this.arbiter;
    }

    public EntityIdentifiable getVictim() {
        return this.victim;
    }

    @Nullable
    public String getReason() {
        return this.reason;
    }
}
