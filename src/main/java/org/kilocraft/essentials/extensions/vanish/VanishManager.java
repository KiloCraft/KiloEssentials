package org.kilocraft.essentials.extensions.vanish;

import org.kilocraft.essentials.api.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VanishManager {
    private List<UUID> vanishedUsers;
    private VanishSettings settings;

    public VanishManager() {
        this.vanishedUsers = new ArrayList<>();
    }

    public VanishManager of(User user) {
        this.vanishedUsers.add(user.getUuid());

        return this;
    }

    public VanishSettings getSettings() {
        return this.settings;
    }

}
