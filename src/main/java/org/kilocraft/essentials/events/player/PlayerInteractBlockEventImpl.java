package org.kilocraft.essentials.events.player;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.kilocraft.essentials.api.event.player.PlayerInteractBlockEvent;

public class PlayerInteractBlockEventImpl implements PlayerInteractBlockEvent {
    private boolean cancelled = false;
    private ServerPlayerEntity player;
    private BlockHitResult hitResult;
    private Hand hand;

    public PlayerInteractBlockEventImpl(ServerPlayerEntity player, BlockHitResult hitResult, Hand hand) {
        this.player = player;
        this.hitResult = hitResult;
        this.hand = hand;
    }

    @Override
    public BlockHitResult getHitResult() {
        return hitResult;
    }

    @Override
    public Hand getHand() {
        return hand;
    }

    @Override
    public ServerPlayerEntity getPlayer() {
        return player;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean isCancelled) {
        this.cancelled = isCancelled;
    }
}
