package org.kilocraft.essentials.extensions.homes;

import org.kilocraft.essentials.user.UserHomeHandler;

public class UnsafeHomeException extends Exception {
    private Home home;
    private UserHomeHandler.Reason reason;

    public UnsafeHomeException(Home home, UserHomeHandler.Reason reason) {
        this.home = home;
        this.reason = reason;
    }

    public Home getHome() {
        return this.home;
    }

    public UserHomeHandler.Reason getReason() {
        return this.reason;
    }
}
