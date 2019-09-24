package org.kilocraft.essentials.craft.events;

import org.kilocraft.essentials.api.Mod;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.serverEvents.ServerEvent$OnCommandExecution;

public class OnCommand implements EventHandler<ServerEvent$OnCommandExecution> {
    @Override
    public void handle(ServerEvent$OnCommandExecution event) {
        Mod.getLogger().info("%s: %s", event.getSource().getName(), event.getCommand());
    }
}
