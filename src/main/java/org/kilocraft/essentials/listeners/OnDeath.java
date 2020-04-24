package org.kilocraft.essentials.listeners;

import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.player.PlayerDeathEvent;

public class OnDeath implements EventHandler<PlayerDeathEvent> {

	@Override
	public void handle(PlayerDeathEvent event) {
		KiloServer.getServer().getOnlineUser(event.getPlayer()).saveLocation();
	}

}
