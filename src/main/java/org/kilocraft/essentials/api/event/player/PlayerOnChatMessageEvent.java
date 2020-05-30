package org.kilocraft.essentials.api.event.player;

import org.kilocraft.essentials.api.event.Event;
import org.kilocraft.essentials.api.event.context.CancellableReasonContext;
import org.kilocraft.essentials.api.event.context.Contextual;
import org.kilocraft.essentials.api.event.context.PlayerContext;
import org.kilocraft.essentials.api.event.context.WorldContext;

public interface PlayerOnChatMessageEvent extends Event, PlayerContext, WorldContext, CancellableReasonContext, Contextual {
    String getMessage();

    void setMessage(final String message);
}
