package org.kilocraft.essentials.mixin.events;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.events.PlayerEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

    @Inject(at = @At("HEAD"), method = "onDeath")
    private void onDeath(DamageSource damageSource, CallbackInfo ci) {
        PlayerEvents.DEATH.invoker().onDeath((ServerPlayerEntity) (Object) this);
    }

    @Inject(at = @At("RETURN"), method = "stopRiding")
    private void onStopRiding(CallbackInfo ci) {
        PlayerEvents.STOP_RIDING.invoker().onStopRiding((ServerPlayerEntity) (Object) this);
    }

}
