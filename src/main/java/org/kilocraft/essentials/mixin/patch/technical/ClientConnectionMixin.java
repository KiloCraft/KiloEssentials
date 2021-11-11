package org.kilocraft.essentials.mixin.patch.technical;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin {

    private static final long MAX_PACKETS_PER_SECOND = 500;
    private int packetsSinceLastInterval = 0;
    private long lastInterval = System.currentTimeMillis();

    @Shadow
    private PacketListener packetListener;

    @Inject(
            method = "channelRead0",
            at = @At("HEAD")
    )
    protected void packetLimit(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
        if (this.packetListener instanceof ServerPlayNetworkHandler networkHandler) {
            if ((System.currentTimeMillis() - this.lastInterval) > 1000) {
                this.packetsSinceLastInterval = 0;
                this.lastInterval = System.currentTimeMillis();
            }
            if (this.packetsSinceLastInterval >= MAX_PACKETS_PER_SECOND) {
                networkHandler.disconnect(new TranslatableText("disconnect.exceeded_packet_rate"));
            }
            this.packetsSinceLastInterval++;
        }
    }


}
