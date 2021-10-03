package org.kilocraft.essentials.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.kilocraft.essentials.api.KiloEssentials;

public class PlayerEvents {

    public static final Event<JoinEvent> JOINED = EventFactory.createArrayBacked(JoinEvent.class, (callbacks) -> (connection, player) -> {
        try {
            for (JoinEvent callback : callbacks) {
                callback.onJoin(connection, player);
            }
        } catch (Exception e) {
            KiloEssentials.getLogger().fatal("Exception occurred when player joined", e);
        }
    });

    public interface JoinEvent {
        void onJoin(ClientConnection connection, ServerPlayerEntity player);
    }

    public static final Event<LeaveEvent> LEAVE = EventFactory.createArrayBacked(LeaveEvent.class, (callbacks) -> (player) -> {
        for (LeaveEvent callback : callbacks) {
            callback.onLeave(player);
        }
    });

    public interface LeaveEvent {
        void onLeave(ServerPlayerEntity player);
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

    public static final Event<InteractBlockEvent> INTERACT_BLOCK = EventFactory.createArrayBacked(InteractBlockEvent.class, (callbacks) -> (player, world, stack, hand, hitResult) -> {
        for (InteractBlockEvent callback : callbacks) {
            ActionResult result = callback.onInteract(player, world, stack, hand, hitResult);
            if (result != ActionResult.PASS) {
                return result;
            }
        }
        return ActionResult.PASS;
    });

    public interface InteractBlockEvent {
        ActionResult onInteract(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult);
    }

    public static final Event<InteractItemEvent> INTERACT_ITEM = EventFactory.createArrayBacked(InteractItemEvent.class, (callbacks) -> (player, world, stack, hand) -> {
        for (InteractItemEvent callback : callbacks) {
            ActionResult result = callback.onInteract(player, world, stack, hand);
            if (result != ActionResult.PASS) {
                return result;
            }
        }
        return ActionResult.PASS;
    });

    public interface InteractItemEvent {
        ActionResult onInteract(ServerPlayerEntity player, World world, ItemStack stack, Hand hand);
    }


}
