package org.kilocraft.essentials.api.event.player;

import org.kilocraft.essentials.api.event.Cancellable;
import org.kilocraft.essentials.api.event.Event;
import org.kilocraft.essentials.api.event.context.PlayerContext;

public interface PlayerDeathEvent extends Event, PlayerContext, Cancellable {
}
