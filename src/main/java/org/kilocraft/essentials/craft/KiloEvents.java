package org.kilocraft.essentials.craft;

import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.craft.events.PlayerJoinEvent;
import org.kilocraft.essentials.craft.events.PlayerLeaveEvent;

public class KiloEvents {
    public KiloEvents() {
        KiloServer.getServer().registerEvent(new PlayerJoinEvent());
        KiloServer.getServer().registerEvent(new PlayerLeaveEvent());
    }
}
