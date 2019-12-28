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
        }};

        for (EventHandler event : events) {
            KiloServer.getServer().registerEvent(event);
        }
//        KiloServer.getServer().registerEvent(new OnServerReady());
//        KiloServer.getServer().registerEvent(new OnSave());
//        KiloServer.getServer().registerEvent(new OnReload());
//        KiloServer.getServer().registerEvent(new PlayerJoinEvent());
//        KiloServer.getServer().registerEvent(new PlayerLeaveEvent());
//        KiloServer.getServer().registerEvent(new OnCommand());
//        KiloServer.getServer().registerEvent(new OnDeath());
//        KiloServer.getServer().registerEvent(new OnTick());
//        KiloServer.getServer().registerEvent(new OnStart());
    }
}
