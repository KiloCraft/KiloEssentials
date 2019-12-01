package org.kilocraft.essentials.mixin.events;

import net.minecraft.server.MinecraftServer;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.events.server.ServerOnStartEventImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer$OnStart {

    @Inject(
            method = "start",
            at = @At(value = "INVOKE", target = "start")
    )
    private void modify(CallbackInfo ci) {
        KiloServer.getServer().triggerEvent(new ServerOnStartEventImpl((MinecraftServer)(Object)this));
    }
}
