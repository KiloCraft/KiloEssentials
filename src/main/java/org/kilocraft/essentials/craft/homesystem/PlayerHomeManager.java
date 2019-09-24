package org.kilocraft.essentials.craft.homesystem;

import org.kilocraft.essentials.craft.config.DataHandler;

public class PlayerHomeManager {
    public PlayerHomeManager() {
        DataHandler.getHomes().load();

    }
}
