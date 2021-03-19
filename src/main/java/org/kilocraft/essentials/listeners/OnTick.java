package org.kilocraft.essentials.listeners;

import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.server.ServerTickEvent;
import org.kilocraft.essentials.events.server.ServerScheduledUpdateEventImpl;
import org.kilocraft.essentials.user.ServerUserManager;
import org.kilocraft.essentials.util.math.DataTracker;
import org.kilocraft.essentials.util.settings.ServerSettings;

public class OnTick implements EventHandler<ServerTickEvent> {
	private int tick = 0;

	@Override
	public void handle(@NotNull ServerTickEvent event) {
        ((ServerUserManager) KiloServer.getServer().getUserManager()).onTick();

        if (tick >= 100) {
            KiloServer.getServer().triggerEvent(new ServerScheduledUpdateEventImpl());
            tick = 0;
        }

        KiloEssentials.getInstance().getFeatures().onTick();
        tick++;
	}

}
