package org.kilocraft.essentials.mixin.entityActivationRange;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.kilocraft.essentials.patch.entityActivationRange.ActivationRange;
import org.kilocraft.essentials.patch.entityActivationRange.InactiveEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/EntityList;forEach(Ljava/util/function/Consumer;)V"))
    public void activateEntities(BooleanSupplier booleanSupplier, CallbackInfo ci) {
        ActivationRange.activateEntities((World) (Object) this);
    }

    @Inject(method = "tickEntity", at = @At(value = "HEAD"), cancellable = true)
    public void onEntityTick(Entity entity, CallbackInfo ci) {
        if (!ActivationRange.checkIfActive(entity)) {
            ++entity.age;
            ((InactiveEntity)entity).inactiveTick();
            ci.cancel();
        }
    }

    @Redirect(method = "tickPassenger", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;tickRiding()V"))
    public void shouldTickPassengers(Entity entity) {
        if (ActivationRange.checkIfActive(entity)) {
            entity.tickRiding();
        } else {
            entity.setVelocity(Vec3d.ZERO);
            ((InactiveEntity)entity).inactiveTick();
            entity.getVehicle().updatePassengerPosition(entity);
        }
    }

}
