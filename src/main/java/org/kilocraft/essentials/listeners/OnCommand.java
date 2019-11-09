package org.kilocraft.essentials.listeners;

import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.commands.ExecuteCommandEvent;

public class OnCommand implements EventHandler<ExecuteCommandEvent> {
    @Override
    public void handle(ExecuteCommandEvent event) {
        ModConstants.getLogger().info("[%s]: %s", event.getSource().getName(), event.getCommand().replace("/", ""));
    }
}
