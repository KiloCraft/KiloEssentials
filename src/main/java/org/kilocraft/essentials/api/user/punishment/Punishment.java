package org.kilocraft.essentials.api.user.punishment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.util.EntityIdentifiable;

import java.util.Date;

public class Punishment extends PunishmentEntry {
    private @Nullable final Date expiry;

    public Punishment(@NotNull final EntityIdentifiable arbiter,
                      @Nullable final EntityIdentifiable victim) {
        this(arbiter, victim, null, null, null);
    }

    public Punishment(@NotNull final EntityIdentifiable arbiter,
                      @Nullable final String victimIP) {
        this(arbiter, null, victimIP, null, null);
    }

    public Punishment(@NotNull final EntityIdentifiable arbiter,
                      @Nullable final EntityIdentifiable victim,
                      @Nullable final String reason) {
        this(arbiter, victim, null, reason, null);
    }

    public Punishment(@NotNull final EntityIdentifiable arbiter,
                      @Nullable final String victimIP,
                      @Nullable final String reason) {
        this(arbiter, null, victimIP, reason, null);
    }

    public Punishment(@NotNull final EntityIdentifiable arbiter,
                      @Nullable final EntityIdentifiable victim,
                      @Nullable final String victimIP,
                      @Nullable final String reason,
                      @Nullable final Date expiry) {
        super(arbiter, victim, victimIP, reason);
        this.expiry = expiry;
    }

    @Nullable
    public Date getExpiry() {
        return this.expiry;
    }

    public enum Type {
        DENY_ACCESS_IP,
        DENY_ACCESS,
        MUTE
    }

    public enum ActionResult {
        SUCCESS,
        FAILED;
    }
}
