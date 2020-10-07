package org.kilocraft.essentials.api.event.player;

import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.event.Event;
import org.kilocraft.essentials.api.event.context.CancellableReasonContext;
import org.kilocraft.essentials.api.event.context.Contextual;
import org.kilocraft.essentials.api.user.OnlineUser;

public interface PlayerOnDirectMessageEvent extends Event, CancellableReasonContext, Contextual {
    String getMessage();

    void setMessage(final String message);

    ServerCommandSource getSource();

    OnlineUser getReceiver();
}
