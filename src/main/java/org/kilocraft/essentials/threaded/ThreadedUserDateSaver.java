package org.kilocraft.essentials.threaded;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.user.UserManager;
import org.kilocraft.essentials.user.OnlineServerUser;

import java.io.IOException;

public class ThreadedUserDateSaver implements KiloThread, Runnable {
    private Logger logger;
    private UserManager userManager;

    public ThreadedUserDateSaver(UserManager userManager) {
        this.userManager = userManager;
    }

    @Override
    public void run() {
        logger = LogManager.getLogger(getName());
        logger.info("Saving users data, this may take a while...");

        for (OnlineServerUser serverUser : this.userManager.getOnlineUsers().values()) {
            try {
                logger.debug("Saving user \"" + serverUser.getUsername() + "\"");
                this.userManager.saveUser(serverUser);
            } catch (IOException e) {
                KiloEssentials.getLogger().error("An unexpected exception occurred when saving a user's data!");
                e.printStackTrace();
            }
        }
        logger.info("Saved the users data!");
    }

    @Override
    public String getName() {
        return "UserHandler";
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
