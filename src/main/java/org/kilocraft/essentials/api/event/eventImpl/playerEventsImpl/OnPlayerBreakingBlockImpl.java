package org.kilocraft.essentials.api.event.eventImpl.playerEventsImpl;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.packet.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import org.kilocraft.essentials.api.event.playerEvents.OnPlayerBreakingBlockEvent;
import org.kilocraft.essentials.api.math.Vec3d;

public class OnPlayerBreakingBlockImpl implements OnPlayerBreakingBlockEvent {

    private PlayerActionC2SPacket packet;
    private PlayerEntity playerEntity;

    private boolean isCancelled;

    public OnPlayerBreakingBlockImpl(PlayerActionC2SPacket packet, ServerPlayerEntity playerEntity) {
        this.packet = packet;
        this.playerEntity = playerEntity;
    }

    public PlayerEntity getPlayer() {
        return playerEntity;
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
        return new Vec3d(packet.getPos().getX(), packet.getPos().getY(), packet.getPos().getZ());
    }

    @Override
    public BlockState getBlockState() {
        return playerEntity.getEntityWorld().getBlockState(new BlockPos(getLocation().getX(), getLocation().getY(), getLocation().getZ()));
    }

    @Override
    public Block getBlock() {
        return getBlockState().getBlock();
    }
}
