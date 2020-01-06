package org.kilocraft.essentials.listeners;

import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.server.lifecycle.ServerReadyEvent;
import org.kilocraft.essentials.events.server.ServerScheduledUpdateEventImpl;
import org.kilocraft.essentials.extensions.warps.WarpCommand;
import org.kilocraft.essentials.provided.BrandedServer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class OnServerReady implements EventHandler<ServerReadyEvent> {
    @Override
    public void handle(ServerReadyEvent event) {
        BrandedServer.set();
        KiloServer.getServer().getMetaManager().load();
        WarpCommand.registerAliases();

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() ->
                KiloServer.getServer().triggerEvent(new ServerScheduledUpdateEventImpl()), 0, 2, TimeUnit.SECONDS);

    }
}
