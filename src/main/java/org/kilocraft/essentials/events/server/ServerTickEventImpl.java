package org.kilocraft.essentials.events.server;

import net.minecraft.server.MinecraftServer;
import org.kilocraft.essentials.api.event.server.ServerTickEvent;

public class ServerTickEventImpl implements ServerTickEvent {
	private final MinecraftServer server;
	public ServerTickEventImpl(MinecraftServer minecraftServer) {
		server = minecraftServer;
	}
	
	@Override
	public MinecraftServer getServer() {
		return server;
	}
}
