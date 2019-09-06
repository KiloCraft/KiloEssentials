package org.kilocraft.essentials.craft;

import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.craft.EventUsageExample;

public class KiloEvents {
    public KiloEvents() {
        KiloServer.getServer().registerEvent(new EventUsageExample());
        KiloServer.getServer().registerEvent(new SecondEventExample());
    }
}
