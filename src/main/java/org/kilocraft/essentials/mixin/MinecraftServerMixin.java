package org.kilocraft.essentials.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.server.Brandable;
import org.kilocraft.essentials.events.server.ServerTickEventImpl;
import org.kilocraft.essentials.util.math.RollingAverage;
import org.kilocraft.essentials.util.TPSTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements Brandable {
    private int currentTick = 0;

    private long currentTime;
    private long tickSection;

    @Shadow private long timeReference;

    @Inject(at = @At(value = "RETURN"), method = "<init>")
    private void kilo$run(CallbackInfo ci) {
        KiloServer.setupServer((MinecraftServer) (Object) this);
        tickSection = System.nanoTime();
    }

    @Inject(at = @At("HEAD"), method = "tick")
    private void ke$onTickStart(BooleanSupplier booleanSupplier, CallbackInfo ci) {
        TPSTracker.MillisecondPerTick.onStart();
        long i = ((currentTime = System.nanoTime()) / (1000L * 1000L)) - this.timeReference;

        if (++currentTick % RollingAverage.SAMPLE_INTERVAL == 0) {
            final long diff = currentTime - tickSection;

            BigDecimal currentTps = RollingAverage.TPS_BASE.divide(new BigDecimal(diff), 30, RoundingMode.HALF_UP);
            TPSTracker.tps1.add(currentTps, diff);
            TPSTracker.tps5.add(currentTps, diff);
            TPSTracker.tps15.add(currentTps, diff);
            TPSTracker.tps30.add(currentTps, diff);
            TPSTracker.tps60.add(currentTps, diff);
            tickSection = currentTime;
        }

        KiloServer.getServer().triggerEvent(new ServerTickEventImpl((MinecraftServer) (Object) this));
    }

    @Inject(at = @At("RETURN"), method = "tick")
    private void ke$onTickReturn(BooleanSupplier booleanSupplier, CallbackInfo ci) {
        TPSTracker.MillisecondPerTick.onEnd();
    }

    @Override
    public String getServerModName() {
        return KiloServer.getServer().getBrandName();
    }

}
