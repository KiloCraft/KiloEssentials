package org.kilocraft.essentials.mixin.events;

import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.events.server.ServerReadyEventImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftDedicatedServer.class)
public abstract class MixinMinecraftDedicatedServer {

        //@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;getMeasuringTimeNano()J", ordinal = 1), method = "setupServer") // Does not work
        private void oky$setupServer$ready(CallbackInfoReturnable<Boolean> cir) {
            KiloServer.getServer().triggerEvent(new ServerReadyEventImpl());
        }

}