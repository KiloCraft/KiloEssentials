package org.kilocraft.essentials.listeners;

import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.commands.OnCommandExecutionEvent;

public class OnCommand implements EventHandler<OnCommandExecutionEvent> {
    @Override
    public void handle(OnCommandExecutionEvent event) {
        ModConstants.getLogger().info("[%s]: %s", event.getExecutor().getName(), event.getCommand().replace("/", ""));
    }
}
