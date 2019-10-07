package org.kilocraft.essentials.api.mixin.event;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.eventImpl.playerEventsImpl.PlayerEvent$OnDeathImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity$PlayerEvent$OnDeath {
	
    @Inject(at = @At("HEAD"), method = "onDeath")
    private void oky$death(DamageSource damageSource_1, CallbackInfo ci) {
        KiloServer.getServer().triggerEvent(new PlayerEvent$OnDeathImpl((ServerPlayerEntity) (Object) this));
    }

}
