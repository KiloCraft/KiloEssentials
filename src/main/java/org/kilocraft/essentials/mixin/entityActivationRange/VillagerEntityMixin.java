package org.kilocraft.essentials.mixin.entityActivationRange;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.world.World;
import org.kilocraft.essentials.patch.entityActivationRange.InactiveEntity;
import org.kilocraft.essentials.util.settings.ServerSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin<E extends LivingEntity> extends MerchantEntity implements InactiveEntity {


    private boolean inactive = false;

    public VillagerEntityMixin(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow
    protected abstract void mobTick();

    @Shadow
    public abstract int getExperience();

    @Shadow
    public abstract void setExperience(int i);

    @Override
    public void inactiveTick() {
        if (this.getExperience() > 0) {
            this.setExperience(this.getExperience() - 1);
        }
        if (ServerSettings.tickInactiveVillagers) {
            inactive = false;
        } else {
            inactive = true;
        }
        this.mobTick();
        ++this.despawnCounter;
        if (this.world.isClient) {
            this.calculateDimensions();
        } else {
            int i = this.getBreedingAge();
            if (i < 0) {
                i++;
                this.setBreedingAge(i);
            } else if (i > 0) {
                i--;
                this.setBreedingAge(i);
            }
        }
    }

    @Redirect(method = "mobTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/VillagerEntity;isAiDisabled()Z"))
    public boolean stopWhenInactive(VillagerEntity villagerEntity) {
        return villagerEntity.isAiDisabled() || inactive;
    }

    @Inject(method = "mobTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/MerchantEntity;mobTick()V"), cancellable = true)
    public void shouldTickSuper(CallbackInfo ci) {
        if (inactive) ci.cancel();
    }



}
