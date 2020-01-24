package org.kilocraft.essentials;

import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.listeners.*;

import java.util.ArrayList;
import java.util.List;

public class KiloEvents {

    public KiloEvents() {
        List<EventHandler> events = new ArrayList<EventHandler>() {{
            add(new OnServerReady());
            add(new OnSave());
            add(new OnReload());
            add(new PlayerJoinEvent());
            add(new PlayerJoinedEvent());
            add(new PlayerLeaveEvent());
            add(new OnCommand());
            add(new OnDeath());
            add(new OnTick());
            add(new OnStart());
            add(new OnScheduledUpdate());
            add(new OnInteractItem());
            add(new OnInteractBlock());
            add(new OnClientCommand());
            add(new OnServerStop());
        }};

        for (EventHandler event : events)
            KiloServer.getServer().registerEvent(event);

    }
}
