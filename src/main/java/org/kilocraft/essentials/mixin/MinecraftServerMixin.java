package org.kilocraft.essentials.mixin;

import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.server.MinecraftServer;
import org.kilocraft.essentials.events.ServerEvents;
import org.kilocraft.essentials.provided.BrandedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Inject(at = @At("HEAD"), method = "tick")
    private void ke$onTickStart(BooleanSupplier booleanSupplier, CallbackInfo ci) {
        ServerEvents.TICK.invoker().onTick();
    }

    @Inject(method = "prepareStartRegion", at = @At(value = "HEAD"), cancellable = true)
    public void noSpawnChunks(CallbackInfo ci) {
        /*if (!ServerSettings.getBoolean("patch.load_spawn"))*/ ci.cancel();
    }

    /**
     * @author Drex
     * @reason Modify server mod name
     */
    @DontObfuscate
    @Overwrite
    public String getServerModName() {
        return BrandedServer.getFinalBrandName();
    }

}
