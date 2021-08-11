package org.kilocraft.essentials.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;

public class ServerEvents {

    public static final Event<ServerReadyEvent> READY = EventFactory.createArrayBacked(ServerReadyEvent.class, (callbacks) -> (server) -> {
        for (ServerReadyEvent callback : callbacks) {
            callback.onReady(server);
        }
    });

    public interface ServerReadyEvent {
        void onReady(MinecraftDedicatedServer server);
    }

    public static final Event<ServerStartedEvent> STARTED = EventFactory.createArrayBacked(ServerStartedEvent.class, (callbacks) -> (server) -> {
        for (ServerStartedEvent callback : callbacks) {
            callback.onStarted(server);
        }
    });

    public interface ServerStartedEvent {
        void onStarted(MinecraftServer server);
    }

    public static final Event<ServerStopEvent> STOPPING = EventFactory.createArrayBacked(ServerStopEvent.class, (callbacks) -> () -> {
        for (ServerStopEvent callback : callbacks) {
            callback.onStop();
        }
    });

    public interface ServerStopEvent {
        void onStop();
    }

    public static final Event<ServerReloadEvent> RELOAD = EventFactory.createArrayBacked(ServerReloadEvent.class, (callbacks) -> (server) -> {
        for (ServerReloadEvent callback : callbacks) {
            callback.onReload(server);
        }
    });

    public interface ServerReloadEvent {
        void onReload(MinecraftServer server);
    }

    public static final Event<ServerSaveEvent> SAVE = EventFactory.createArrayBacked(ServerSaveEvent.class, (callbacks) -> (server) -> {
        for (ServerSaveEvent callback : callbacks) {
            callback.onSave(server);
        }
    });

    public interface ServerSaveEvent {
        void onSave(MinecraftServer server);
    }

    public static final Event<ServerTickEvent> TICK = EventFactory.createArrayBacked(ServerTickEvent.class, (callbacks) -> () -> {
        for (ServerTickEvent callback : callbacks) {
            callback.onTick();
        }
    });

    public interface ServerTickEvent {
        void onTick();
    }

}
