package org.kilocraft.essentials.mixin.patch.performance;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Inject(method = "prepareStartRegion", at = @At(value = "HEAD"), cancellable = true)
    public void noSpawnChunks(CallbackInfo ci) {
        /*if (!ServerSettings.getBoolean("patch.load_spawn"))*/
        ci.cancel();
    }

}
