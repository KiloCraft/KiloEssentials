package org.kilocraft.essentials.listeners;

import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.server.lifecycle.ServerReloadEvent;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.provided.BrandedServer;

public class OnReload implements EventHandler<ServerReloadEvent> {
    @Override
    public void handle(ServerReloadEvent event) {
        KiloConfig.load();
        KiloCommands.registerToast();
        KiloCommands.updateCommandTreeForEveryone();
        BrandedServer.load();
        KiloServer.getServer().getMetaManager().load();
        KiloServer.getServer().getMetaManager().updateAll();
    }
}
