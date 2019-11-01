package org.kilocraft.essentials.craft.events;

import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.playerEvents.PlayerEvent$OnDeath;
import org.kilocraft.essentials.craft.commands.essentials.BackCommand;

import net.minecraft.client.util.math.Vector3f;

public class OnDeath implements EventHandler<PlayerEvent$OnDeath> {

	@Override
	public void handle(PlayerEvent$OnDeath event) {
		BackCommand.setLocation(event.getPlayer(),
				new Vector3f((float) event.getPlayer().getPos().x, (float) event.getPlayer().getPos().y, (float) event.getPlayer().getPos().z), event.getPlayer().dimension);
	}

}
