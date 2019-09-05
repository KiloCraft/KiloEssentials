package org.kilocraft.essentials.api.mixin;

import net.minecraft.server.MinecraftServer;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.util.RollingAverage;
import org.kilocraft.essentials.api.util.SomeGlobals;
import org.kilocraft.essentials.api.wrapper.ServerBrand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer implements ServerBrand {
    private int currentTick = 0;

    private long curTime;
    private long tickSection;

    @Shadow
    private long timeReference;

    @Inject(at = @At(value = "HEAD"), method = "run")
    private void kilo$run(CallbackInfo ci) {
        long start = System.nanoTime();

        tickSection = start;
    }

    @Inject(at = @At("HEAD"), method = "tick")
    private void oky$tick(BooleanSupplier booleanSupplier_1, CallbackInfo ci) {
        long i = ((curTime = System.nanoTime()) / (1000L * 1000L)) - this.timeReference;

        if (++currentTick % RollingAverage.SAMPLE_INTERVAL == 0) {
            final long diff = curTime - tickSection;

            java.math.BigDecimal currentTps = RollingAverage.TPS_BASE.divide(new java.math.BigDecimal(diff), 30, java.math.RoundingMode.HALF_UP);
            SomeGlobals.tps1.add(currentTps, diff);
            SomeGlobals.tps5.add(currentTps, diff);
            SomeGlobals.tps15.add(currentTps, diff);
            tickSection = curTime;
        }
    }


    @Override
    public String getServerModName() {
        return KiloServer.getServer().getDisplayBrandName();
    }

}
