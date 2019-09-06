package org.kilocraft.essentials.api.entity.entityImpl;

import net.minecraft.network.MessageType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.hit.HitResult;
import org.kilocraft.essentials.api.entity.Player;
import org.kilocraft.essentials.api.math.Vec3d;
import org.kilocraft.essentials.api.util.EntityServerRayTraceable;
import org.kilocraft.essentials.api.world.Block;

import java.util.Optional;

public class PlayerImpl extends EntityImpl implements Player {

    private final ServerPlayerEntity player;

    public PlayerImpl(ServerPlayerEntity player) {
        super(player);

        this.player = player;
    }

    @Override
    public Optional<Block> getTargetBlock(double distance, boolean returnFluids) {
        HitResult res = ((EntityServerRayTraceable) player).rayTraceInServer(distance, 1.0f, returnFluids); // 1.0f = unknown

        if (res.getType() != HitResult.Type.BLOCK)
            return Optional.empty();

        return Optional.ofNullable(getWorld().getBlockAt(new Vec3d(res.getPos().x, res.getPos().y, res.getPos().z)));
    }

    @Override
    public void send(String message) {
        send(message, MessageType.CHAT);
    }

    @Override
    public void send(String message, MessageType type) {
        send(message, type);
    }

    @Override
    public void send(LiteralText message) {
        send(message);
    }

    @Override
    public void kick(String message) {
        player.networkHandler.disconnect(new LiteralText(message));
    }

    @Override
    public boolean isOp() {
        return player.server.getPlayerManager().isOperator(player.getGameProfile());
    }


    @Override
    public void setOp(boolean isOp) {
        if (isOp)
            player.server.getPlayerManager().addToOperators(player.getGameProfile());
        else
            player.server.getPlayerManager().removeFromOperators(player.getGameProfile());
    }

}
