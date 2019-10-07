package org.kilocraft.essentials.api.mixin.event;

import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.eventImpl.serverEventsImpl.ServerEvent$OnStopImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer$OnStop {

    @Final
    @Shadow
    private static Logger LOGGER;

    @Inject(at = @At("HEAD"), method = "shutdown")
    private void modify$shutdown(CallbackInfo ci) {
        LOGGER.info("KiloAPI: Shutting down the server");
        KiloServer.getServer().triggerEvent(new ServerEvent$OnStopImpl());
    }

    @Inject(at = @At("HEAD"), method = "close")
    private void modify$close(CallbackInfo ci) {
        KiloServer.getServer().shutdown();
    }
}
