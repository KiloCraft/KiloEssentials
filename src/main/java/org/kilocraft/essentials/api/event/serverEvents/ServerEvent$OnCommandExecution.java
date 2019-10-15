package org.kilocraft.essentials.api.event.serverEvents;

import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.event.Cancellable;
import org.kilocraft.essentials.api.event.Event;

public interface ServerEvent$OnCommandExecution extends Event, ServerEvent, Cancellable {
    String getCommand();

    ServerCommandSource getSource();
}
