package org.kilocraft.essentials.events.player;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.kilocraft.essentials.api.event.player.PlayerInteractItem;

public class PlayerInteractItemEventImpl implements PlayerInteractItem {
    private boolean cancelled = false;
    private PlayerEntity player;
    private World world;
    private Hand hand;
    private ItemStack itemStack;
    private ActionResult actionResult;

    public PlayerInteractItemEventImpl(PlayerEntity playerEntity, World world, Hand hand, ItemStack itemStack) {
        this.player = playerEntity;
        this.world = world;
        this.hand = hand;
        this.itemStack = itemStack;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean isCancelled) {
        this.cancelled = isCancelled;
    }

    @Override
    public ServerPlayerEntity getPlayer() {
        return (ServerPlayerEntity) this.player;
    }

    @Override
    public ServerWorld getWorld() {
        return (ServerWorld) this.world;
    }

    @Override
    public ItemStack getItemStack() {
        return this.itemStack;
    }

    @Override
    public Hand getHand() {
        return this.hand;
    }

    @Override
    public void setReturnValue(ActionResult actionResult) {
        this.actionResult = actionResult;
    }

    @Override
    public ActionResult getReturnValue() {
        return this.actionResult;
    }
}
