package org.kilocraft.essentials.mixin.events;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.player.PlayerStopRidingEvent;
import org.kilocraft.essentials.events.player.PlayerDeathEventImpl;
import org.kilocraft.essentials.events.player.PlayerStopRidingEventImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity$PlayerEvents {

    @Inject(at = @At("HEAD"), method = "onDeath")
    private void ke$triggerEvent$onDeath(DamageSource damageSource, CallbackInfo ci) {
        KiloServer.getServer().triggerEvent(new PlayerDeathEventImpl((ServerPlayerEntity) (Object) this));
    }

    @Inject(at = @At("RETURN"), method = "stopRiding", cancellable = true)
    private void stopRiding(CallbackInfo ci) {
        PlayerStopRidingEvent event = new PlayerStopRidingEventImpl((ServerPlayerEntity) (Object) this);
        KiloServer.getServer().triggerEvent(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }

}
