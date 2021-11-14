package org.kilocraft.essentials.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerEvents {

    /**
     * An event for notification when the player is completed connection.
     *
     * <p>This will run after {@link net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents#JOIN}.
     */
    public static final Event<PlayerReadyEvent> PLAYER_READY = EventFactory.createArrayBacked(PlayerReadyEvent.class, (callbacks) -> (connection, player) -> {
        for (PlayerReadyEvent callback : callbacks) {
            callback.onPlayerReady(connection, player);
        }
    });

    public interface PlayerReadyEvent {
        void onPlayerReady(ClientConnection connection, ServerPlayerEntity player);
    }

    public static final Event<StopRidingEvent> STOP_RIDING = EventFactory.createArrayBacked(StopRidingEvent.class, (callbacks) -> (player) -> {
        for (StopRidingEvent callback : callbacks) {
            callback.onStopRiding(player);
        }
    });

    public interface StopRidingEvent {
        void onStopRiding(ServerPlayerEntity player);
    }

    public static final Event<DeathEvent> DEATH = EventFactory.createArrayBacked(DeathEvent.class, (callbacks) -> (player) -> {
        for (DeathEvent callback : callbacks) {
            callback.onDeath(player);
        }
    });

    public interface DeathEvent {
        void onDeath(ServerPlayerEntity player);
    }

}
