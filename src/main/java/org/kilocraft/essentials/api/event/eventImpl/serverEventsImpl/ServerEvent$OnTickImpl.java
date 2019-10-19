package org.kilocraft.essentials.api.event.eventImpl.serverEventsImpl;

import org.kilocraft.essentials.api.event.serverEvents.ServerEvent$OnTick;

import net.minecraft.server.MinecraftServer;

public class ServerEvent$OnTickImpl implements ServerEvent$OnTick {

	MinecraftServer server;
	
	public ServerEvent$OnTickImpl (MinecraftServer minecraftServer) {
		server = minecraftServer;
	}
	
	@Override
	public MinecraftServer getServer() {
		return server;
	}

}
