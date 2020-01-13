package org.kilocraft.essentials.listeners;

import org.kilocraft.essentials.KiloEssentialsImpl;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.server.lifecycle.ServerReadyEvent;
import org.kilocraft.essentials.extensions.warps.WarpCommand;
import org.kilocraft.essentials.provided.BrandedServer;

public class OnServerReady implements EventHandler<ServerReadyEvent> {
    @Override
    public void handle(ServerReadyEvent event) {
        KiloEssentialsImpl.getInstance().onServerReady();

        BrandedServer.set();
        KiloServer.getServer().getMetaManager().load();
        WarpCommand.registerAliases();
    }
}
