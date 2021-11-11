package org.kilocraft.essentials.mixin.events;

import net.minecraft.server.MinecraftServer;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.events.ServerEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    // TODO: Use https://github.com/FabricMC/fabric/tree/1.17/fabric-lifecycle-events-v1 instead
    @Inject(method = "runServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;setFavicon(Lnet/minecraft/server/ServerMetadata;)V",
                    ordinal = 0
            )
    )
    private void onStarted(CallbackInfo info) {
        ServerEvents.STARTED.invoker().onStarted((MinecraftServer) (Object) this);
    }

    @Inject(
            method = "save",
            at = @At(
                    value = "HEAD",
                    target = "Lnet/minecraft/server/MinecraftServer;save(ZZZ)Z"
            )
    )
    private void onSave(boolean bl, boolean bl2, boolean bl3, CallbackInfoReturnable<Boolean> cir) {
        ServerEvents.SAVE.invoker().onSave((MinecraftServer) (Object) this);
    }

    @Inject(
            method = "shutdown",
            at = @At("HEAD")
    )
    private void onShutdown(CallbackInfo ci) {
        KiloEssentials.getLogger().info("Shutting down the dedicated server...");
        ServerEvents.STOPPING.invoker().onStop();
    }

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void onTickStart(BooleanSupplier booleanSupplier, CallbackInfo ci) {
        ServerEvents.TICK.invoker().onTick();
    }

}
