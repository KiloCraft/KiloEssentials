package org.kilocraft.essentials.mixin.entityActivationRange;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.world.World;
import org.kilocraft.essentials.patch.entityActivationRange.InactiveEntity;
import org.kilocraft.essentials.util.settings.ServerSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin extends MerchantEntity implements InactiveEntity {

    @Shadow protected abstract void mobTick();

    public VillagerEntityMixin(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public void inactiveTick() {
        if (ServerSettings.tickInactiveVillagers) {
            this.mobTick();
        }
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
}
