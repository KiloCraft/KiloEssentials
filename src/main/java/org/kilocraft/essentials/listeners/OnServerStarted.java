package org.kilocraft.essentials.listeners;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.server.lifecycle.ServerStartedEvent;

import java.util.concurrent.CompletableFuture;

public class OnServerStarted implements EventHandler<ServerStartedEvent> {

    @Override
    public void handle(@NotNull ServerStartedEvent event) {
        CompletableFuture.runAsync(() -> {
            LuckPerms luckPerms = null;
            while (luckPerms == null) {
                try {
                    luckPerms = LuckPermsProvider.get();
                } catch (IllegalStateException ignored) {}
            }
            LuckPermsListener listener = new LuckPermsListener(luckPerms);
        });

    }
}
