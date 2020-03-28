package org.kilocraft.essentials.listeners;

import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.KiloDebugUtils;
import org.kilocraft.essentials.KiloEssentialsImpl;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.server.lifecycle.ServerReloadEvent;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.provided.BrandedServer;
import org.kilocraft.essentials.util.NBTStorageUtil;

public class OnReload implements EventHandler<ServerReloadEvent> {
    @Override
    public void handle(ServerReloadEvent event) {
        KiloConfig.reload();
        KiloCommands.updateCommandTreeForEveryone();
        BrandedServer.load();
        KiloServer.getServer().getMetaManager().load();
        KiloServer.getServer().getMetaManager().updateAll();

        KiloEssentials.getInstance().getFeatures().loadAll();
        KiloEssentialsImpl.getInstance().onServerLoad();

        NBTStorageUtil.onSave();

        try {
            KiloDebugUtils.validateDebugMode(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
