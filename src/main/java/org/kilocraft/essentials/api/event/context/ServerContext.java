package org.kilocraft.essentials.api.event.context;

import net.minecraft.server.MinecraftServer;

/**
 * Represents a context which involves a server.
 */
public interface ServerContext {
    MinecraftServer getServer();
}
