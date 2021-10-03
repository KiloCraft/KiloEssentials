package org.kilocraft.essentials.mixin.events;

import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import org.kilocraft.essentials.events.ServerEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftDedicatedServer.class)
public abstract class MinecraftDedicatedServerMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;getMeasuringTimeNano()J", ordinal = 1), method = "setupServer")
    private void onServerReady(CallbackInfoReturnable<Boolean> cir) {
        ServerEvents.READY.invoker().onReady((MinecraftDedicatedServer) (Object) this);
    }
}

