package org.kilocraft.essentials.mixin.entityActivationRange;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.patch.entityActivationRange.ActivationRange;
import org.kilocraft.essentials.patch.entityActivationRange.ActivationTypeEntity;
import org.kilocraft.essentials.patch.entityActivationRange.InactiveEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin implements ActivationTypeEntity, InactiveEntity {

    @Shadow public abstract void setVelocity(Vec3d vec3d);

    @Shadow public World world;

    @Shadow public abstract Vec3d getVelocity();

    public boolean defaultActivationState;
    public ActivationRange.ActivationType activationType;
    public int activatedTick = Integer.MIN_VALUE;
    public boolean isTemporarilyActive = false;

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    public void onEntityInit(EntityType<?> entityType, World world, CallbackInfo ci) {
        activationType = ActivationRange.initializeEntityActivationType((Entity) (Object) this);
        if (world != null) {
            this.defaultActivationState = ActivationRange.initializeEntityActivationState((Entity) (Object) this);
        } else {
            this.defaultActivationState = false;
        }
    }

    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;adjustMovementForPiston(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;"))
    public void onPistonMove(MovementType movementType, Vec3d vec3d, CallbackInfo ci) {
        this.activatedTick = KiloEssentials.getServer().getMinecraftServer().getTicks() + 20;
    }

    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;adjustMovementForSneaking(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/entity/MovementType;)Lnet/minecraft/util/math/Vec3d;"), cancellable = true)
    public void ignoreMovementWhileInactive(MovementType movementType, Vec3d vec3d, CallbackInfo ci) {
        if (isTemporarilyActive && !((Object)this instanceof ItemEntity || (Object)this instanceof AbstractMinecartEntity) && vec3d == getVelocity() && movementType == MovementType.SELF) {
            setVelocity(Vec3d.ZERO);
            this.world.getProfiler().pop();
            ci.cancel();
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

    @Override
    public boolean isTemporarilyActive() {
        return isTemporarilyActive;
    }

    @Override
    public void setTemporarilyActive(boolean temporarilyActive) {
        isTemporarilyActive = temporarilyActive;
    }
}
