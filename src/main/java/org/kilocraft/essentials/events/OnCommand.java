package org.kilocraft.essentials.events;

import org.kilocraft.essentials.api.ModData;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.serverEvents.ServerEvent$OnCommandExecution;

public class OnCommand implements EventHandler<ServerEvent$OnCommandExecution> {
    @Override
    public void handle(ServerEvent$OnCommandExecution event) {
        ModData.getLogger().info("[%s]: %s", event.getSource().getName(), event.getCommand().replace("/", ""));
    }
}
