package org.kilocraft.essentials.listeners;

import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.server.lifecycle.ServerSaveEvent;
import org.kilocraft.essentials.util.nbt.NBTStorageUtil;

public class OnSave implements EventHandler<ServerSaveEvent> {
    @Override
    public void handle(@NotNull ServerSaveEvent event) {
        NBTStorageUtil.onSave();
        KiloServer.getServer().getUserManager().saveAllUsers();
    }
}
