package org.kilocraft.essentials.mixin.events;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.events.PlayerEvents;
import org.kilocraft.essentials.util.InteractionHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

    // TODO: Use https://github.com/FabricMC/fabric/tree/1.17/fabric-entity-events-v1 instead
    @Inject(
            at = @At("HEAD"),
            method = "onDeath"
    )
    private void onDeath(DamageSource damageSource, CallbackInfo ci) {
        PlayerEvents.DEATH.invoker().onDeath((ServerPlayerEntity) (Object) this);
    }

    @Inject(
            method = "stopRiding",
            at = @At("RETURN")
    )
    private void onStopRiding(CallbackInfo ci) {
        PlayerEvents.STOP_RIDING.invoker().onStopRiding((ServerPlayerEntity) (Object) this);
    }

    // TODO: Use https://github.com/FabricMC/fabric/tree/1.17/fabric-events-interaction-v0 instead
    @Inject(
            method = "attack",
            at = @At("HEAD")
    )
    public void onAttack(Entity entity, CallbackInfo ci) {
        InteractionHandler.handleInteraction((ServerPlayerEntity) (Object) this, entity, true);
    }

}
