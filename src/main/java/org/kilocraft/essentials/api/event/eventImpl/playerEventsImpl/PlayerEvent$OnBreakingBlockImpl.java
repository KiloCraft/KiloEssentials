package org.kilocraft.essentials.api.event.eventImpl.playerEventsImpl;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.packet.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.kilocraft.essentials.api.event.playerEvents.PlayerEvent$OnBreakingBlockEvent;

public class PlayerEvent$OnBreakingBlockImpl implements PlayerEvent$OnBreakingBlockEvent {

    private PlayerActionC2SPacket packet;
    private ServerPlayerEntity playerEntity;

    private boolean isCancelled;

    public PlayerEvent$OnBreakingBlockImpl(PlayerActionC2SPacket packet, ServerPlayerEntity playerEntity) {
        this.packet = packet;
        this.playerEntity = playerEntity;
    }

    public ServerPlayerEntity getPlayer() {
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
        return playerEntity.getEntityWorld().getBlockState(new BlockPos(packet.getPos().getX(), packet.getPos().getY(), packet.getPos().getZ()));
    }

    @Override
    public Block getBlock() {
        return playerEntity.getEntityWorld().getBlockState(new BlockPos(packet.getPos().getX(), packet.getPos().getY(), packet.getPos().getZ())).getBlock();
    }
}
