package org.kilocraft.essentials.listeners;

import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.KiloEssentialsImpl;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.server.lifecycle.ServerStopEvent;
import org.kilocraft.essentials.provided.LocateBiomeProvided;
import org.kilocraft.essentials.votifier.Votifier;

public class OnServerStop implements EventHandler<ServerStopEvent> {
    @Override
    public void handle(@NotNull ServerStopEvent event) {
        Votifier.getInstance().onDisable();
        KiloEssentialsImpl.getInstance().onServerStop();
        LocateBiomeProvided.stopAll();
    }
}
