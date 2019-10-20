package org.kilocraft.essentials.craft;

import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.craft.events.*;

public class KiloEvents {
    public KiloEvents() {
        KiloServer.getServer().registerEvent(new PlayerJoinEvent());
        KiloServer.getServer().registerEvent(new PlayerLeaveEvent());
        KiloServer.getServer().registerEvent(new OnCommand());
        KiloServer.getServer().registerEvent(new OnDeath());
        KiloServer.getServer().registerEvent(new OnReload());
        KiloServer.getServer().registerEvent(new OnTick());
    }
}
