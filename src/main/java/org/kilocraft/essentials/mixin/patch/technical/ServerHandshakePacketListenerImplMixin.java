package org.kilocraft.essentials.mixin.patch.technical;

import net.minecraft.SharedConstants;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerHandshakePacketListenerImpl;
import org.kilocraft.essentials.patch.technical.VersionCompatibility;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerHandshakePacketListenerImpl.class)
public abstract class ServerHandshakePacketListenerImplMixin {

    @Shadow
    @Final
    private MinecraftServer server;

    @Inject(
            method = "handleIntention",
            at = @At("HEAD")
    )
    public void onRequest(ClientIntentionPacket packet, CallbackInfo ci) {
        if (packet.getIntention() == ConnectionProtocol.STATUS && this.server.repliesToStatus() && VersionCompatibility.isEnabled()) {
            VersionCompatibility.onHandshake(packet);
        }
    }

    @Redirect(
            method = "handleIntention",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/protocol/handshake/ClientIntentionPacket;getProtocolVersion()I",
                    ordinal = 0
            )
    )
    public int onLogin(ClientIntentionPacket handshakeC2SPacket) {
        if (handshakeC2SPacket.getProtocolVersion() == VersionCompatibility.getPretendMetaVersion().getProtocol()) {
            return SharedConstants.getCurrentVersion().getProtocolVersion();
        }
        return handshakeC2SPacket.getProtocolVersion();
    }

}
