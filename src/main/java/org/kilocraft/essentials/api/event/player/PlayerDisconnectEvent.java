package org.kilocraft.essentials.api.event.player;

import org.kilocraft.essentials.api.event.Event;
import org.kilocraft.essentials.api.event.context.PlayerContext;
import org.kilocraft.essentials.api.event.context.ServerContext;

public interface PlayerDisconnectEvent extends Event, PlayerContext, ServerContext {
}
