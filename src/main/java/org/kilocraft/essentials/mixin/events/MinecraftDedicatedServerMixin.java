package org.kilocraft.essentials.mixin.events;

import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import org.kilocraft.essentials.events.ServerEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftDedicatedServer.class)
public abstract class MinecraftDedicatedServerMixin {

    // TODO: Use https://github.com/FabricMC/fabric/tree/1.17/fabric-lifecycle-events-v1 instead
    @Inject(method = "setupServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Util;getMeasuringTimeNano()J",
                    ordinal = 1
            )
    )
    private void onServerReady(CallbackInfoReturnable<Boolean> cir) {
        ServerEvents.READY.invoker().onReady((MinecraftDedicatedServer) (Object) this);
    }

}

