package org.kilocraft.essentials.mixin;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ServerResourceManager;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.util.UserCache;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.server.Brandable;
import org.kilocraft.essentials.events.server.ServerTickEventImpl;
import org.kilocraft.essentials.patch.entityActivationRange.ActivationRange;
import org.kilocraft.essentials.util.math.DataTracker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Proxy;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements Brandable {

    @Inject(at = @At(value = "RETURN"), method = "<init>")
    private void kilo$run(CallbackInfo ci) {
        KiloServer.setupServer((MinecraftServer) (Object) this);
    }

    @Inject(at = @At("HEAD"), method = "tick")
    private void ke$onTickStart(BooleanSupplier booleanSupplier, CallbackInfo ci) {
        DataTracker.tps.add((long) (1000L / Math.max(50, DataTracker.getMSPT())));
        KiloServer.getServer().triggerEvent(new ServerTickEventImpl((MinecraftServer) (Object) this));
    }

    @Inject(at = @At("RETURN"), method = "tick")
    private void ke$onTickReturn(BooleanSupplier booleanSupplier, CallbackInfo ci) {
        //TpsTracker.MillisecondPerTick.onEnd();
    }

    @Inject(method = "prepareStartRegion", at = @At(value = "HEAD"), cancellable = true)
    public void noSpawnChunks(CallbackInfo ci) {
        ci.cancel();
    }

    @Override
    public String getServerModName() {
        return KiloServer.getServer().getBrandName();
    }

}
