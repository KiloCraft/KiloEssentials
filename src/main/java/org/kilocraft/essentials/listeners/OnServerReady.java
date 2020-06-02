package org.kilocraft.essentials.listeners;

import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.server.lifecycle.ServerReadyEvent;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.extensions.warps.serverwidewarps.WarpCommand;
import org.kilocraft.essentials.provided.BrandedServer;
import org.kilocraft.essentials.user.ServerUserManager;
import org.kilocraft.essentials.util.nbt.NBTStorageUtil;

public class OnServerReady implements EventHandler<ServerReadyEvent> {
    @Override
    public void handle(@NotNull ServerReadyEvent event) {
        EssentialCommand.onServerReady();
        NBTStorageUtil.onLoad();
        BrandedServer.set();
        KiloServer.getServer().getMetaManager().load();
        WarpCommand.registerAliases();
        KiloServer.getServer().setName(KiloConfig.main().server().name);
        ((ServerUserManager) KiloServer.getServer().getUserManager()).onServerReady();
    }
}
