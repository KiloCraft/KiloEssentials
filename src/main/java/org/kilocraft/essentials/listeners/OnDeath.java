package org.kilocraft.essentials.listeners;

import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.player.PlayerDeathEvent;
import org.kilocraft.essentials.commands.teleport.BackCommand;

import net.minecraft.client.util.math.Vector3f;

public class OnDeath implements EventHandler<PlayerDeathEvent> {

	@Override
	public void handle(PlayerDeathEvent event) {
		BackCommand.setLocation(event.getPlayer(),
				new Vector3f((float) event.getPlayer().getPos().x, (float) event.getPlayer().getPos().y, (float) event.getPlayer().getPos().z), event.getPlayer().dimension);
	}

}
