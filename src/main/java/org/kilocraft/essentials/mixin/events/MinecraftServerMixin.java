package org.kilocraft.essentials.mixin.events;

import net.minecraft.server.MinecraftServer;
import org.kilocraft.essentials.events.ServerEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Inject(
            method = "saveAllChunks",
            at = @At("HEAD")
    )
    private void saveAllEvent(boolean bl, boolean bl2, boolean bl3, CallbackInfoReturnable<Boolean> cir) {
        ServerEvents.SAVE.invoker().onSave((MinecraftServer) (Object) this);
    }

}
