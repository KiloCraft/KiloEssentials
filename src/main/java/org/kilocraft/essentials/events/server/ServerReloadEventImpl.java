package org.kilocraft.essentials.events.server;

import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.event.server.lifecycle.ServerReloadEvent;

public class ServerReloadEventImpl implements ServerReloadEvent {
    private final MinecraftServer server;
    public ServerReloadEventImpl(@NotNull final MinecraftServer server) {
        this.server = server;
    }

    @Override
    public MinecraftServer getServer() {
        return this.server;
    }
}
