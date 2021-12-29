package org.kilocraft.essentials.mixin.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.kilocraft.essentials.events.PlayerEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

    @Inject(
            at = @At("HEAD"),
            method = "die"
    )
    private void onDeath(DamageSource damageSource, CallbackInfo ci) {
        PlayerEvents.DEATH.invoker().onDeath((ServerPlayer) (Object) this);
    }

    @Inject(
            method = "stopRiding",
            at = @At("RETURN")
    )
    private void onStopRiding(CallbackInfo ci) {
        PlayerEvents.STOP_RIDING.invoker().onStopRiding((ServerPlayer) (Object) this);
    }

}
