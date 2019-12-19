package org.kilocraft.essentials.events.player;

import net.minecraft.block.Block;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.kilocraft.essentials.api.event.player.PlayerPlaceBlockEvent;

public class PlayerPlaceBlockEventImpl implements PlayerPlaceBlockEvent {

    private final ServerPlayerEntity player;
    private final ItemUsageContext context;
    private final Block block;
    private boolean cancelled;

    public PlayerPlaceBlockEventImpl(ItemUsageContext itemUsageContext_1, ServerPlayerEntity player, Block block) {
        this.context = itemUsageContext_1;
        this.player = player;
        this.block = block;
    }

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

    @Override
    public Vec3d getLocation() {
        BlockPos pos = context.getBlockPos();
        return new Vec3d(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public ServerWorld getWorld() {
        return player.getServerWorld();
    }
}
