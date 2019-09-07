package org.kilocraft.essentials.api.mixin.event;

import net.minecraft.client.network.packet.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.packet.PlayerActionC2SPacket;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.eventImpl.playerEventsImpl.PlayerEvent$OnBreakingBlockImpl;
import org.kilocraft.essentials.api.server.Server;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class MixinServerPlayerNetworkHandler$PlayerEvent$OnBreakingBlock {

    @Shadow
    public ServerPlayerEntity player;

    @Inject(at = @At("HEAD"), method = "onPlayerAction", cancellable = true)
    private void oky$onPlayerAction(PlayerActionC2SPacket playerActionC2SPacket_1, CallbackInfo ci) {
        if (!player.isCreative())
            if (playerActionC2SPacket_1.getAction() != PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK)
                return;

        Server s = KiloServer.getServer();
        if (!s.isMainThread())
            return;

        PlayerEvent$OnBreakingBlockImpl e = s.triggerEvent(new PlayerEvent$OnBreakingBlockImpl(playerActionC2SPacket_1, player));
        if (e.isCancelled()) {
            this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(player.world, playerActionC2SPacket_1.getPos()));
            ci.cancel();
        }
    }
}
