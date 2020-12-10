package org.kilocraft.essentials.listeners;

import net.luckperms.api.LuckPermsProvider;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.server.lifecycle.ServerStartedEvent;

public class OnServerStarted implements EventHandler<ServerStartedEvent> {

    @Override
    public void handle(@NotNull ServerStartedEvent event) {
        LuckPermsListener listener = new LuckPermsListener(LuckPermsProvider.get());
    }
}
