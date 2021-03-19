package org.kilocraft.essentials.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.MathHelper;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.server.Brandable;
import org.kilocraft.essentials.events.server.ServerTickEventImpl;
import org.kilocraft.essentials.util.math.DataTracker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements Brandable {

    @Shadow
    @Final
    public long[] lastTickLengths;

    @Shadow private int ticks;

    @Inject(at = @At(value = "RETURN"), method = "<init>")
    private void kilo$run(CallbackInfo ci) {
        KiloServer.setupServer((MinecraftServer) (Object) this);
    }

    @Inject(at = @At("HEAD"), method = "tick")
    private void ke$onTickStart(BooleanSupplier booleanSupplier, CallbackInfo ci) {
        DataTracker.tps.add((long) (1000L / Math.max(50, DataTracker.getMSPT())));
        DataTracker.compute();

        KiloServer.getServer().triggerEvent(new ServerTickEventImpl((MinecraftServer) (Object) this));
    }

    @Inject(at = @At("RETURN"), method = "tick")
    private void ke$onTickReturn(BooleanSupplier booleanSupplier, CallbackInfo ci) {
        //TpsTracker.MillisecondPerTick.onEnd();
    }

    @Override
    public String getServerModName() {
        return KiloServer.getServer().getBrandName();
    }

}
