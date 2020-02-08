package org.kilocraft.essentials.listeners;

import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.server.ServerTickEvent;
import org.kilocraft.essentials.events.server.ServerScheduledUpdateEventImpl;
import org.kilocraft.essentials.extensions.magicalparticles.ParticleAnimationManager;
import org.kilocraft.essentials.user.ServerUserManager;

public class OnTick implements EventHandler<ServerTickEvent> {
	private int tick = 0;

	@Override
	public void handle(ServerTickEvent event) {
		((ServerUserManager) KiloServer.getServer().getUserManager()).onTick();
		ParticleAnimationManager.onTick();

		if (tick >= 100) {
			KiloServer.getServer().triggerEvent(new ServerScheduledUpdateEventImpl());
			tick = 0;
		}

		tick++;
	}

}
