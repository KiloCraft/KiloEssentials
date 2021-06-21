package org.kilocraft.essentials.mixin;

import net.minecraft.SharedConstants;
import net.minecraft.network.NetworkState;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerHandshakeNetworkHandler;
import org.kilocraft.essentials.servermeta.ServerMetaManager;
import org.kilocraft.essentials.util.settings.ServerSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerHandshakeNetworkHandler.class)
public class ServerHandshakeNetworkHandlerMixin {
    @Shadow @Final private MinecraftServer server;

    @Inject(method = "onHandshake", at = @At("HEAD"))
    public void onRequest(HandshakeC2SPacket packet, CallbackInfo ci) {
        if (packet.getIntendedState() == NetworkState.STATUS && this.server.acceptsStatusQuery()) {
            ServerMetaManager.cachedProtocolVersion = packet.getProtocolVersion();
        }
    }

    @Redirect(method = "onHandshake(Lnet/minecraft/network/packet/c2s/handshake/HandshakeC2SPacket;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/c2s/handshake/HandshakeC2SPacket;getProtocolVersion()I"))
    public int onLogin(HandshakeC2SPacket handshakeC2SPacket) {
        if (handshakeC2SPacket.getProtocolVersion() == ServerSettings.releaseProtocolVersion) {
            return SharedConstants.getGameVersion().getProtocolVersion();
        }
        return handshakeC2SPacket.getProtocolVersion();
    }
}
