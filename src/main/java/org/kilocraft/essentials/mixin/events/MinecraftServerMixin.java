package org.kilocraft.essentials.mixin.events;

import net.minecraft.server.MinecraftServer;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.events.ServerEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;setFavicon(Lnet/minecraft/server/ServerMetadata;)V", ordinal = 0), method = "runServer")
    private void oky$setupServer$done(CallbackInfo info) {
        ServerEvents.STARTED.invoker().onStarted((MinecraftServer) (Object) this);
    }

    @Inject(method = "save", at = @At(value = "HEAD", target = "Lnet/minecraft/server/MinecraftServer;save(ZZZ)Z"))
    private void modify(boolean bl, boolean bl2, boolean bl3, CallbackInfoReturnable<Boolean> cir) {
        ServerEvents.SAVE.invoker().onSave((MinecraftServer) (Object) this);
    }

    @Inject(at = @At("HEAD"), method = "shutdown")
    private void modify$shutdown(CallbackInfo ci) {
        KiloEssentials.getLogger().info("Shutting down the dedicated server...");
        ServerEvents.STOPPING.invoker().onStop();
    }

}
