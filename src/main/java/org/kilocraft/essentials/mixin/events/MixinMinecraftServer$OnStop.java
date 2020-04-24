package org.kilocraft.essentials.mixin.events;

import net.minecraft.server.MinecraftServer;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.events.server.ServerStopEventImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer$OnStop {

    @Inject(at = @At("HEAD"), method = "shutdown")
    private void modify$shutdown(CallbackInfo ci) {
        KiloEssentials.getLogger().info("Shutting down the dedicated server...");
        KiloServer.getServer().triggerEvent(new ServerStopEventImpl());
    }

    @Inject(at = @At("HEAD"), method = "close")
    private void modify$close(CallbackInfo ci) {
        KiloServer.getServer().shutdown();
    }
}
