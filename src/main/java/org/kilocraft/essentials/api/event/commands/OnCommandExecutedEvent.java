package org.kilocraft.essentials.api.event.commands;

import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.event.Cancellable;
import org.kilocraft.essentials.api.event.Event;
import org.kilocraft.essentials.api.event.context.ServerContext;

public interface OnCommandExecutedEvent extends Event, ServerContext, Cancellable {
    String getCommand();

    ServerCommandSource getExecutor();
}
