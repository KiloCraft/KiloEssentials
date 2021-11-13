package org.kilocraft.essentials.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerEvents {

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
