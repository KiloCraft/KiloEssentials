package org.kilocraft.essentials.listeners;

import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.KiloDebugUtils;
import org.kilocraft.essentials.KiloEssentialsImpl;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.server.lifecycle.ServerReloadEvent;
import org.kilocraft.essentials.chat.ServerChat;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.provided.BrandedServer;
import org.kilocraft.essentials.util.nbt.NBTStorageUtil;

public class OnReload implements EventHandler<ServerReloadEvent> {
    @Override
    public void handle(@NotNull ServerReloadEvent event) {
        try {
            KiloConfig.reload();
            BrandedServer.load();
            KiloServer.getServer().getMetaManager().load();
            KiloServer.getServer().getMetaManager().updateAll();
            ServerChat.load();

            KiloEssentials.getInstance().getFeatures().loadAll();
            KiloEssentialsImpl.getInstance().onServerLoad();

            NBTStorageUtil.onSave();

            KiloDebugUtils.validateDebugMode(true);
        } catch (Exception e) {
            KiloEssentials.getLogger().error("An unexpected error occurred while reloading the server!", e);
        }

        KiloCommands.updateCommandTreeForEveryone();
    }
}
