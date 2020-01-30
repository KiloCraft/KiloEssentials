package org.kilocraft.essentials.listeners;

import net.minecraft.SharedConstants;
import org.kilocraft.essentials.KiloDebugUtils;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.server.lifecycle.ServerScheduledUpdateEvent;
import org.kilocraft.essentials.extensions.betterchairs.PlayerSitManager;

public class OnScheduledUpdate implements EventHandler<ServerScheduledUpdateEvent> {
    @Override
    public void handle(ServerScheduledUpdateEvent event) {
        KiloServer.getServer().getMetaManager().updateAll();
        PlayerSitManager.INSTANCE.onScheduledUpdate();

        if (SharedConstants.isDevelopment) {
            KiloDebugUtils.INSTANCE.onScheduledUpdate();
        }
    }
}
