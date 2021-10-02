package org.kilocraft.essentials.mixin.patch.technical;

import net.minecraft.SharedConstants;
import net.minecraft.network.NetworkState;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerHandshakeNetworkHandler;
import org.kilocraft.essentials.patch.technical.VersionCompability;
import org.kilocraft.essentials.util.settings.ServerSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerHandshakeNetworkHandler.class)
public abstract class ServerHandshakeNetworkHandlerMixin {

    @Shadow
    @Final
    private MinecraftServer server;

    @Inject(
            method = "onHandshake",
            at = @At("HEAD")
    )
    public void onRequest(HandshakeC2SPacket packet, CallbackInfo ci) {
        if (packet.getIntendedState() == NetworkState.STATUS && this.server.acceptsStatusQuery()) {
            VersionCompability.onHandshake(packet);
        }
    }

    @Redirect(
            method = "onHandshake(Lnet/minecraft/network/packet/c2s/handshake/HandshakeC2SPacket;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/packet/c2s/handshake/HandshakeC2SPacket;getProtocolVersion()I",
                    ordinal = 0
            )
    )
    public int onLogin(HandshakeC2SPacket handshakeC2SPacket) {
        if (handshakeC2SPacket.getProtocolVersion() == 1073741867) {
            return SharedConstants.getGameVersion().getProtocolVersion();
        }
        return handshakeC2SPacket.getProtocolVersion();
    }

}
