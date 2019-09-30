package org.kilocraft.essentials.api.event.serverEvents;

import net.minecraft.server.MinecraftServer;

public interface ServerEvent {
    MinecraftServer getServer();
}
