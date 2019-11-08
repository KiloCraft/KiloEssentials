package org.kilocraft.essentials.api.mixin.event;

import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.eventImpl.serverEventsImpl.ServerEvent$OnReadyImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftDedicatedServer.class)
public abstract class MixinMinecraftDedicatedServer {

        @Inject(at = @At(value = "INVOKE", target = "net.minecraft.util.SystemUtil.getMeasuringTimeNano()J", ordinal = 1), method = "setupServer")
        private void oky$setupServer$ready(CallbackInfoReturnable<Boolean> cir) {
            KiloServer.getServer().triggerEvent(new ServerEvent$OnReadyImpl());
        }

}