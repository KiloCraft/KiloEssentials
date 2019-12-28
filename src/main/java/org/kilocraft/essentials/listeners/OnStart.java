package org.kilocraft.essentials.listeners;

import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.server.lifecycle.ServerStartEvent;
import org.kilocraft.essentials.provided.BrandedServer;

;

public class OnStart implements EventHandler<ServerStartEvent>{

	@Override
	public void handle(ServerStartEvent event) {
		BrandedServer.set();
	}

}
