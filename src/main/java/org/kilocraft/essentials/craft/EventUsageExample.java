package org.kilocraft.essentials.craft;

import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.eventImpl.ReadyEventImpl;

public class EventUsageExample extends ReadyEventImpl {
    static {
        KiloServer.getServer().getLogger().info("Example event usage! Runned by the ReadyEvent");
    }
}
