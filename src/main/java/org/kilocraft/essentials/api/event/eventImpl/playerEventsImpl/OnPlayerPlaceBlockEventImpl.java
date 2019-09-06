package org.kilocraft.essentials.api.event.eventImpl.playerEventsImpl;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.kilocraft.essentials.api.entity.entityImpl.PlayerImpl;
import org.kilocraft.essentials.api.event.playerEvents.OnPlayerPlaceBlockEvent;
import org.kilocraft.essentials.api.math.Vec3d;
import org.kilocraft.essentials.api.world.Block;

public class OnPlayerPlaceBlockEventImpl implements OnPlayerPlaceBlockEvent {

    private final PlayerImpl player;
    private final ItemUsageContext context;
    private final Block block;


    private boolean isCancelled;

    public OnPlayerPlaceBlockEventImpl(ItemUsageContext itemUsageContext_1, PlayerEntity player, Block block) {
        this.context = itemUsageContext_1;
        this.player = new PlayerImpl((ServerPlayerEntity) player);
        this.block = block;
    }

    public PlayerImpl getPlayer() {
        return player;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
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
}
