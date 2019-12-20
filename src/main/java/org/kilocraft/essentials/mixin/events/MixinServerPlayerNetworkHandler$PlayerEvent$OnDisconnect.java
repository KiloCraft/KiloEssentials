package org.kilocraft.essentials.mixin.events;

import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.player.PlayerDisconnectEvent;
import org.kilocraft.essentials.events.player.PlayerDisconnectEventImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class MixinServerPlayerNetworkHandler$PlayerEvent$OnDisconnect {

    @Shadow
    public ServerPlayerEntity player;

    @Shadow @Final private static Logger LOGGER;

    @Inject(at = @At("HEAD"), method = "onDisconnected")
    private void oky$remove(Text text_1, CallbackInfo ci) {
        PlayerDisconnectEvent event = KiloServer.getServer().triggerEvent(new PlayerDisconnectEventImpl(player));
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendToAll(Lnet/minecraft/text/Text;)V"), method = "onDisconnected")
    private void oky$remove$sendToAll(PlayerManager playerManager, Text text_1) {
        LOGGER.info("{} lost connection: {}", this.player.getName(), text_1.toString());
    }

}
