package org.kilocraft.essentials.mixin.events;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.kilocraft.essentials.util.InteractionHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Shadow
    public abstract Text getName();

    // TODO: Use https://github.com/FabricMC/fabric/tree/1.17/fabric-events-interaction-v0 instead
    @Inject(
            method = "interact",
            at = @At("HEAD")
    )
    public void onInteract(Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        InteractionHandler.handleInteraction((ServerPlayerEntity) (Object) this, entity, false);
    }

}
