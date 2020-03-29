package org.kilocraft.essentials.mixin.patch;

import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerHandSwingPatch {

    @Redirect(method = "onPlayerInteractBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;swingHand(Lnet/minecraft/util/Hand;Z)V"))
    private void patch$InteractBlock$LivingEntity$SwingHand(LivingEntity livingEntity, Hand hand, boolean bl) {
        livingEntity.swingHand(hand);
    }

    @Redirect(method = "onPlayerInteractEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;swingHand(Lnet/minecraft/util/Hand;Z)V"))
    private void patch$InteractEntity$LivingEntity$SwingHand(LivingEntity livingEntity, Hand hand, boolean bl) {
        livingEntity.swingHand(hand);
    }
}
