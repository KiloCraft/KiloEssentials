package org.kilocraft.essentials.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.MinecraftServer;

public class ServerEvents {

    public static final Event<ServerSaveEvent> SAVE = EventFactory.createArrayBacked(ServerSaveEvent.class, (callbacks) -> (server) -> {
        for (ServerSaveEvent callback : callbacks) {
            callback.onSave(server);
        }
    });

    public interface ServerSaveEvent {
        void onSave(MinecraftServer server);
    }

}
