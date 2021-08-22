package org.kilocraft.essentials.mixin.patch.technical;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.text.LiteralText;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.kilocraft.essentials.api.KiloEssentials;
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

    @Inject(method = "channelRead0", at = @At("HEAD"), cancellable = true)
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
        if (packetListener instanceof ServerPlayNetworkHandler networkHandler) {
            if ((System.currentTimeMillis() - lastInterval) > 1000) {
                packetsSinceLastInterval = 0;
                lastInterval = System.currentTimeMillis();
            }
            if (packetsSinceLastInterval >= MAX_PACKETS_PER_SECOND) {
                networkHandler.disconnect(new LiteralText("Too Many Packets!"));
            }
            packetsSinceLastInterval++;
        }
    }

    @Inject(method = "exceptionCaught", at = @At("HEAD"))
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable, CallbackInfo cb) {
        throwable.printStackTrace();
    }
}
