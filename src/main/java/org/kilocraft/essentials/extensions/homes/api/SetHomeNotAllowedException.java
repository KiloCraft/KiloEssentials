package org.kilocraft.essentials.extensions.homes.api;

import org.kilocraft.essentials.user.UserHomeHandler;

public class SetHomeNotAllowedException extends Exception {
    private UserHomeHandler.Reason reason;

    public SetHomeNotAllowedException() {
        this.reason = this.reason = UserHomeHandler.Reason.NO_PERMISSION;
    }

    public UserHomeHandler.Reason getReason() {
        return this.reason;
    }
}
