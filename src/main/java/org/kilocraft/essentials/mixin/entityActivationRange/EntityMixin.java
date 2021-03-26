package org.kilocraft.essentials.mixin.entityActivationRange;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;
import org.kilocraft.essentials.patch.entityActivationRange.ActivationRange;
import org.kilocraft.essentials.patch.entityActivationRange.ActivationTypeEntity;
import org.kilocraft.essentials.patch.entityActivationRange.InactiveEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin implements ActivationTypeEntity, InactiveEntity {

    public boolean defaultActivationState;
    public ActivationRange.ActivationType activationType;
    public int activatedTick = Integer.MIN_VALUE;

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    public void onEntityInit(EntityType<?> entityType, World world, CallbackInfo ci) {
        activationType = ActivationRange.initializeEntityActivationType((Entity) (Object) this);
        if (world != null) {
            this.defaultActivationState = ActivationRange.initializeEntityActivationState((Entity) (Object) this);
        } else {
            this.defaultActivationState = false;
        }
    }

    @Override
    public ActivationRange.ActivationType getActivationType() {
        return activationType;
    }

    @Override
    public boolean getDefaultActivationState() {
        return defaultActivationState;
    }

    @Override
    public int getActivatedTick() {
        return activatedTick;
    }

    @Override
    public void setActivatedTick(int activatedTick) {
        this.activatedTick = activatedTick;
    }

    @Override
    public void inactiveTick() {

    }
}
