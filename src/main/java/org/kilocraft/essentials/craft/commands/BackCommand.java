package org.kilocraft.essentials.craft.commands;

import java.util.HashMap;

import net.minecraft.client.util.math.Vector3f;
import net.minecraft.server.network.ServerPlayerEntity;

public class BackCommand {

	public static HashMap<ServerPlayerEntity, Vector3f> backLocations = new HashMap<ServerPlayerEntity, Vector3f>();

	public static void setLocation(ServerPlayerEntity player, Vector3f position) {
		if (backLocations.containsKey(player)) {
			backLocations.replace(player, position);
		} else {
			backLocations.put(player, position);
		}
	}
	
}
