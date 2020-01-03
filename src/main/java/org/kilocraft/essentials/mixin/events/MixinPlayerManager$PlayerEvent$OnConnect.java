package org.kilocraft.essentials.mixin.events;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.events.player.PlayerConnectEventImpl;
import org.kilocraft.essentials.events.player.PlayerConnectedEventImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public abstract class MixinPlayerManager$PlayerEvent$OnConnect {

    @Shadow @Final private static Logger LOGGER;

    @Shadow @Final private MinecraftServer server;

    @Shadow public abstract int getMaxPlayerCount();

    @Shadow private int viewDistance;

    @Inject(at = @At("HEAD"), method = "onPlayerConnect", cancellable = true)
    private void oky$onPlayerConnect(ClientConnection connection, ServerPlayerEntity playerEntity, CallbackInfo ci) {
        PlayerConnectEventImpl e = KiloServer.getServer().triggerEvent(new PlayerConnectEventImpl(connection, playerEntity));

        if (e.isCancelled()) {
            connection.disconnect(new LiteralText(e.getCancelReason()));
            ci.cancel();
        }
    }


    @Inject(at = @At("TAIL"), method = "onPlayerConnect")
    private void onPlayerConnected(ClientConnection connection, ServerPlayerEntity playerEntity, CallbackInfo ci) {
        PlayerConnectedEventImpl e = KiloServer.getServer().triggerEvent(new PlayerConnectedEventImpl(connection, playerEntity));
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendToAll(Lnet/minecraft/text/Text;)V"), method = "onPlayerConnect")
    private void oky$onPlayerConnect$sendToAll(PlayerManager playerManager, Text text_1) {
        //Ignored
    }

    @Redirect(at = @At(value = "INVOKE",
            target = "Lorg/apache/logging/log4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V"),
            method = "onPlayerConnect")
    private void oky$onPlayerConnect$sendToAll(Logger logger, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        //Ignored
    }

}
