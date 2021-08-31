package org.kilocraft.essentials.mixin.patch.performance.entityActivationRange;

import net.minecraft.block.BedBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.kilocraft.essentials.patch.ChunkManager;
import org.kilocraft.essentials.patch.entityActivationRange.InactiveEntity;
import org.kilocraft.essentials.util.settings.ServerSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Inspired by: Purpur
 * https://github.com/pl3xgaming/Purpur/blob/ver/1.17.1/patches/server/0133-Lobotomize-stuck-villagers.patch
 * Copied from:
 * https://github.com/Wesley1808/ServerCore-Fabric/blob/1.17.1/src/main/java/org/provim/servercore/mixin/performance/VillagerEntityMixin.java
 */

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin extends MerchantEntity implements InactiveEntity {

    private boolean inactive = false;

    private boolean slowed = false;

    public VillagerEntityMixin(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow
    protected abstract void mobTick();

    @Shadow
    public abstract int getExperience();

    // Entity Activation Range
    @Override
    public void inactiveTick() {
        if (this.getHeadRollingTimeLeft() > 0) {
            this.setHeadRollingTimeLeft(this.getHeadRollingTimeLeft() - 1);
        }
        inactive = !ServerSettings.tickInactiveVillagers;
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

    // Entity Activation Range
    @Redirect(method = "mobTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/VillagerEntity;isAiDisabled()Z"))
    public boolean stopWhenInactive(VillagerEntity villagerEntity) {
        return villagerEntity.isAiDisabled() || inactive;
    }

    @Redirect(method = "mobTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/brain/Brain;tick(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/LivingEntity;)V"))
    public void shouldTickBrain(Brain<VillagerEntity> brain, ServerWorld world, LivingEntity entity) {
        // Lobotomize Villagers
        if (ServerSettings.patch_lobotomize_villagers_enabled && this.age % ServerSettings.patch_lobotomize_villagers_tick_interval != 0)
            inactive = inactive || isSlowed();

        // Entity Activation Range
        if (!inactive) brain.tick(world, (VillagerEntity) (Object) this);
    }

    // Lobotomize villagers
    private boolean isSlowed() {
        if (this.age % ServerSettings.patch_lobotomize_villagers_update_interval == 0) {
            this.slowed = !canTravel(this.getBlockPos());
        }

        return this.slowed;
    }

    // Lobotomize villagers
    private boolean canTravel(BlockPos pos) {
        return canTravelTo(pos.east()) || canTravelTo(pos.west()) || canTravelTo(pos.north()) || canTravelTo(pos.south());
    }

    // Lobotomize villagers
    private boolean canTravelTo(BlockPos pos) {
        // Returns true in case its surrounded by any bed. This way we don't break iron farms.
        if (ChunkManager.getStateIfVisible(this.getEntityWorld(), pos).getBlock() instanceof BedBlock) {
            return true;
        }

        Path path = this.getNavigation().findPathTo(pos, 0);
        return path != null && path.reachesTarget();
    }

}
