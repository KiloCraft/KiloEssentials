package org.kilocraft.essentials.api.user.punishment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.util.UserEntity;

public abstract class PunishmentEntry {
    private final UserEntity arbiter, victim;
    private @Nullable
    final String reason;

    public PunishmentEntry(@NotNull final UserEntity arbiter, @NotNull final UserEntity victim, @Nullable final String reason) {
        this.arbiter = arbiter;
        this.victim = victim;
        this.reason = reason;
    }

    public UserEntity getArbiter() {
        return this.arbiter;
    }

    public UserEntity getVictim() {
        return this.victim;
    }

    @Nullable
    public String getReason() {
        return this.reason;
    }
}
