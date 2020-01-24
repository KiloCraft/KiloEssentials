package org.kilocraft.essentials.events.player;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import org.kilocraft.essentials.api.event.player.PlayerOnHandSwingEvent;

public class PlayerOnHandSwingEventImpl implements PlayerOnHandSwingEvent {
    private boolean cancelled = false;
    private ServerPlayerEntity player;
    private Hand hand;

    public PlayerOnHandSwingEventImpl(ServerPlayerEntity player, Hand hand) {
        this.player = player;
        this.hand = hand;
    }

    @Override
    public Hand getHand() {
        return hand;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean isCancelled) {
        this.cancelled = isCancelled;
    }

    @Override
    public ServerPlayerEntity getPlayer() {
        return player;
    }
}
