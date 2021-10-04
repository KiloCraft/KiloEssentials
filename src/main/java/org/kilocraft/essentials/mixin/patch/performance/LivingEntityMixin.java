package org.kilocraft.essentials.mixin.patch.performance;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.kilocraft.essentials.patch.performance.Collidable;
import org.kilocraft.essentials.util.settings.ServerSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements Collidable {

    protected int numCollisions = 0;

    @Redirect(method = "tickCramming", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I", ordinal = 2))
    public int limitEntityCollisions(List<Entity> list) {
        // Stop loop by returning -1 for it's size, if the number of collisions exceeds the maximum
        return this.numCollisions < ServerSettings.patch_maxCollisionsPerEntity ? list.size() : -1;
    }

    @Override
    public void onCollision() {
        this.numCollisions++;
    }

    @Inject(method = "tickCramming", at = @At("HEAD"))
    public void resetEntityCollision(CallbackInfo ci) {
        this.numCollisions = Math.max(0, this.numCollisions - ServerSettings.patch_maxCollisionsPerEntity);
    }

    @Inject(method = "tickCramming", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;pushAway(Lnet/minecraft/entity/Entity;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void onEntityCollision(CallbackInfo ci, List<Entity> entitiesInBoundingBox, int maxEntityCramming, int index, Entity pushedEntity) {
        // Increment collision count for the other entity
        if (pushedEntity instanceof Collidable collidable) collidable.onCollision();
        // Increment collision count for self
        this.numCollisions++;
    }

}
