package org.kilocraft.essentials.listeners;

import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.player.PlayerDeathEvent;
import org.kilocraft.essentials.user.ServerUser;
import org.kilocraft.essentials.user.ServerUserManager;

public class OnDeath implements EventHandler<PlayerDeathEvent> {
	@Override
	public void handle(@NotNull PlayerDeathEvent event) {
		((ServerUserManager) KiloServer.getServer().getUserManager()).onDeath(KiloServer.getServer().getOnlineUser(event.getPlayer()));
	}

}
