package org.kilocraft.essentials.events.server;

import org.kilocraft.essentials.api.event.Event;
import org.kilocraft.essentials.api.event.context.ServerContext;

import net.minecraft.server.MinecraftServer;

public class ServerOnStartEventImpl implements Event, ServerContext {

	MinecraftServer server;
	
	public ServerOnStartEventImpl (MinecraftServer server) {
		this.server = server;
	}
	
	@Override
	public MinecraftServer getServer() {
		return server;
	}

}
