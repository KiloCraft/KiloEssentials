package org.kilocraft.essentials.mixin.events;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.kilocraft.essentials.util.InteractionHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Inject(
            method = "interactAt",
            at = @At("HEAD")
    )
    public void onInteractAt(PlayerEntity playerEntity, Vec3d vec3d, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        InteractionHandler.handleInteraction((ServerPlayerEntity) playerEntity, (Entity) (Object) this, false);
    }
}
