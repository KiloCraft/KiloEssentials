package org.kilocraft.essentials.craft.mixin;

import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftDedicatedServer.class)
public abstract class MixinMinecraftDedicatedServer$setupServer {
    @Inject(at = @At(value = "INVOKE", target = "net.minecraft.util.SystemUtil.getMeasuringTimeNano()J", ordinal = 1), method = "setupServer")
    public void mixin$setupServer(CallbackInfoReturnable<Boolean> cir) {
            
    }
}
