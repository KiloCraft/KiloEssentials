package org.kilocraft.essentials.listeners;

import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.KiloEssentialsImpl;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.server.lifecycle.ServerStopEvent;
import org.kilocraft.essentials.provided.LocateBiomeProvided;
import org.kilocraft.essentials.extensions.votifier.Votifier;

public class OnServerStop implements EventHandler<ServerStopEvent> {
    @Override
    public void handle(@NotNull ServerStopEvent event) {
        KiloEssentialsImpl.getInstance().onServerStop();
        LocateBiomeProvided.stopAll();
        Votifier.getInstance().onDisable();
    }
}
