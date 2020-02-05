package org.kilocraft.essentials.listeners;

import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.server.lifecycle.ServerReloadEvent;
import org.kilocraft.essentials.extensions.warps.WarpManager;
import org.kilocraft.essentials.provided.BrandedServer;
import org.kilocraft.essentials.util.NBTStorageUtil;

public class OnReload implements EventHandler<ServerReloadEvent> {
    @Override
    public void handle(ServerReloadEvent event) {
        KiloCommands.updateCommandTreeForEveryone();
        WarpManager.load();
        BrandedServer.load();
        KiloServer.getServer().getMetaManager().load();
        KiloServer.getServer().getMetaManager().updateAll();

        NBTStorageUtil.onSave();
    }
}
