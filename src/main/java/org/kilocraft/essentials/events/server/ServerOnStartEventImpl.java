package org.kilocraft.essentials.events.server;

import org.kilocraft.essentials.api.event.server.lifecycle.ServerStartEvent;

import net.minecraft.server.MinecraftServer;

public class ServerOnStartEventImpl implements ServerStartEvent {

	MinecraftServer server;
	
	public ServerOnStartEventImpl (MinecraftServer server) {
		this.server = server;
	}
	
	@Override
	public MinecraftServer getServer() {
		return server;
	}

}
