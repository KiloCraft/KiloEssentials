package org.kilocraft.essentials.api.event.serverEvents;

import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.event.Event;

public interface ServerEvent$OnCommandExecution extends Event, ServerEvent {
    String getCommand();

    ServerCommandSource getSource();
}
