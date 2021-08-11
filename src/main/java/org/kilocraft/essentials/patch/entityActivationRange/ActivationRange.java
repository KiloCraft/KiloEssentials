package org.kilocraft.essentials.patch.entityActivationRange;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.*;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.mixin.entityActivationRange.EntityAccessor;
import org.kilocraft.essentials.mixin.entityActivationRange.LivingEntityAccessor;
import org.kilocraft.essentials.mixin.entityActivationRange.MobEntityAccessor;
import org.kilocraft.essentials.mixin.entityActivationRange.PersistentProjectileEntityAccessor;
import org.kilocraft.essentials.util.settings.ServerSettings;

public class ActivationRange {

    static Box maxBB = new Box(0, 0, 0, 0, 0, 0);
    static Activity[] VILLAGER_PANIC_IMMUNITIES = {Activity.HIDE, Activity.PRE_RAID, Activity.RAID, Activity.PANIC};
    public static int[][] activationRange = new int[ActivationRange.ActivationType.values().length][4];

    /**
     * Initializes an entities type on construction to specify what group this
     * entity is in for activation ranges.
     *
     * @param entity
     * @return group id
     */
    public static ActivationType initializeEntityActivationType(Entity entity) {
        if (entity instanceof RaiderEntity) {
            return ActivationType.RAIDER;
        } else if (entity instanceof WaterCreatureEntity) {
            return ActivationType.WATER;
        } else if (entity instanceof VillagerEntity) {
            return ActivationType.VILLAGER;
        } else if (entity instanceof FlyingEntity && entity instanceof Monster) {
            return ActivationType.FLYING_MONSTER;
        } else if (entity instanceof Monster) {
            return ActivationType.MONSTER;
        } else if (entity instanceof PathAwareEntity || entity instanceof AmbientEntity) {
            return ActivationType.ANIMAL;
        } else {
            return ActivationType.MISC;
        }
    }

    /**
     * These entities are excluded from Activation range checks.
     *
     * @param entity
     * @return boolean If it should always tick.
     */
    public static boolean initializeEntityActivationState(Entity entity) {
        ActivationType activationType = ((ActivationTypeEntity) entity).getActivationType();
        if (
                activationRange[activationType.ordinal()][0] <= 0
                        || entity instanceof EyeOfEnderEntity
                        || entity instanceof PlayerEntity
                        || entity instanceof ProjectileEntity
                        || entity instanceof EnderDragonEntity
                        || entity instanceof EnderDragonPart
                        || entity instanceof WitherEntity
                        || entity instanceof GhastEntity //KiloEssentials
                        || entity instanceof FireballEntity
                        || entity instanceof LightningEntity
                        || entity instanceof TntEntity
                        || entity instanceof EndCrystalEntity
                        || entity instanceof FireworkRocketEntity
                        || entity instanceof TridentEntity) {
            return true;
        }

        return false;
    }

    /**
     * Find what entities are in range of the players in the world and set
     * active if in range.
     *
     * @param world
     */
    public static void activateEntities(World world) {
        int maxRange = Integer.MIN_VALUE;
        for (ActivationType activationType : ActivationType.values()) {
            maxRange = Math.max(activationRange[activationType.ordinal()][0], maxRange);
        }
        maxRange = Math.min((ServerSettings.getViewDistance() << 4) - 8, maxRange);
        for (PlayerEntity player : world.getPlayers()) {
            if (player.isSpectator()) continue;
            ((ActivationTypeEntity) player).setActivatedTick(KiloEssentials.getMinecraftServer().getTicks());
            maxBB = player.getBoundingBox().expand(maxRange, 256, maxRange);
            for (ActivationType activationType : ActivationType.values()) {
                activationType.boundingBox = player.getBoundingBox().expand(activationRange[activationType.ordinal()][0], 256, activationRange[activationType.ordinal()][0]);
            }

            for (Entity entity : world.getOtherEntities(player, maxBB)) {
                activateEntity(entity);
            }
        }
    }

    /**
     * Checks for the activation state of all entities in this chunk.
     *
     * @param entity
     */
    private static void activateEntity(Entity entity) {
        ActivationTypeEntity activationTypeEntity = (ActivationTypeEntity) entity;
        if (KiloEssentials.getMinecraftServer().getTicks() > activationTypeEntity.getActivatedTick()) {

            if (activationTypeEntity.getDefaultActivationState() || activationTypeEntity.getActivationType().boundingBox.intersects(entity.getBoundingBox())) {
                activationTypeEntity.setActivatedTick(KiloEssentials.getMinecraftServer().getTicks());
            }
        }
    }

    /**
     * If an entity is not in range, do some more checks to see if we should
     * give it a shot.
     *
     * @param entity
     * @return
     */
    public static int checkEntityImmunities(Entity entity) {
        int inactiveWakeUpImmunity = checkInactiveWakeup(entity);
        if (inactiveWakeUpImmunity > -1) {
            return inactiveWakeUpImmunity;
        }
        if (entity.getFireTicks() > 0) {
            return 2;
        }
        ActivationTypeEntity activationTypeEntity = (ActivationTypeEntity) entity;
        long inactiveFor = KiloEssentials.getMinecraftServer().getTicks() - activationTypeEntity.getActivatedTick();
        // quick checks.
        if (activationTypeEntity.getActivationType() != ActivationType.WATER && !(entity instanceof FlyingEntity) && entity.isSubmergedInWater()) {
            return 100;
        }
        if (!(entity instanceof ArrowEntity)) {
            if ((!entity.isOnGround() && !(entity instanceof FlyingEntity))) {
                return 10;
            }
        } else if (!((PersistentProjectileEntityAccessor) entity).isInGround()) {
            return 1;
        }
        // special cases.
        if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) entity;
            if (living.isClimbing() || ((LivingEntityAccessor) living).isJumping() || living.hurtTime > 0 || living.getStatusEffects().size() > 0) {
                return 1;
            }
            if (entity instanceof MobEntity && ((MobEntity) entity).getTarget() != null) {
                return 20;
            }
            if (entity instanceof BeeEntity) {
                BeeEntity bee = (BeeEntity) entity;
                BlockPos movingTarget = ((EntityWithTarget) bee).getMovingTarget();
                if (bee.getAngryAt() != null ||
                        (bee.getHivePos() != null && bee.getHivePos().equals(movingTarget)) ||
                        (bee.getFlowerPos() != null && bee.getFlowerPos().equals(movingTarget))
                ) {
                    return 20;
                }
            }
            if (entity instanceof VillagerEntity) {
                Brain<VillagerEntity> behaviorController = ((VillagerEntity) entity).getBrain();

                if (ServerSettings.villagerActiveForPanic) {
                    for (Activity activity : VILLAGER_PANIC_IMMUNITIES) {
                        if (behaviorController.hasActivity(activity)) {
                            return 20 * 5;
                        }
                    }
                }

                if (ServerSettings.villagerWorkImmunityAfter > 0 && inactiveFor >= ServerSettings.villagerWorkImmunityAfter) {
                    if (behaviorController.hasActivity(Activity.WORK)) {
                        return ServerSettings.villagerWorkImmunityFor;
                    }
                }
            }
            if (entity instanceof LlamaEntity && ((LlamaEntity) entity).isFollowing()) {
                return 1;
            }
            if (entity instanceof AnimalEntity) {
                AnimalEntity animal = (AnimalEntity) entity;
                if (animal.isBaby() || animal.isInLove()) {
                    return 5;
                }
                if (entity instanceof SheepEntity && ((SheepEntity) entity).isSheared()) {
                    return 1;
                }
            }
            if (entity instanceof CreeperEntity && ((CreeperEntity) entity).isIgnited()) { // isExplosive
                return 20;
            }
            if (entity instanceof MobEntity && ((GoalSelectorInterface) ((MobEntityAccessor) entity).getTargetSelector()).hasTasks()) {
                return 0;
            }
        }
        return -1;
    }

    /**
     * Checks if the entity is active for this tick.
     *
     * @param entity
     * @return
     */
    public static boolean checkIfActive(Entity entity) {

        if (entity instanceof FireworkRocketEntity) {
            return true;
        }

        ActivationTypeEntity activationTypeEntity = (ActivationTypeEntity) entity;
        if (activationTypeEntity.getDefaultActivationState() || entity.age < 20 * 10 || !entity.isAlive() || ((EntityAccessor) entity).isInNetherPortal() || entity.hasNetherPortalCooldown()) {
            return true;
        }

        if (entity instanceof MobEntity && ((MobEntity)entity).getHoldingEntity() instanceof PlayerEntity) {
            return true;
        }

        boolean isActive = activationTypeEntity.getActivatedTick() >= KiloEssentials.getMinecraftServer().getTicks() || activationTypeEntity.getDefaultActivationState();
        ((InactiveEntity)entity).setTemporarilyActive(false);

        // Should this entity tick?
        if (!isActive) {
            if ((KiloEssentials.getMinecraftServer().getTicks() - activationTypeEntity.getActivatedTick() - 1) % 20 == 0) {
                int immunity = checkEntityImmunities(entity);
                if (immunity >= 0) {
                    activationTypeEntity.setActivatedTick(KiloEssentials.getMinecraftServer().getTicks() + immunity);
                } else {
                    ((InactiveEntity)entity).setTemporarilyActive(true);
                }
                isActive = true;
            }
            // Add a little performance juice to active entities. Skip 1/4 if not immune.
        } else if (entity.age % 4 == 0 && checkEntityImmunities(entity) < 0) {
            isActive = false;
        }
        return isActive;
    }

    private static int checkInactiveWakeup(Entity entity) {
        World world = entity.world;
        ActivationTypeEntity activationTypeEntity = (ActivationTypeEntity) entity;
        ActivationType activationType = activationTypeEntity.getActivationType();
        long inactiveFor = KiloEssentials.getMinecraftServer().getTicks() - activationTypeEntity.getActivatedTick();
        if (((WorldInfo) world).getRemaining()[activationType.ordinal()] > 0 && inactiveFor > activationRange[activationType.ordinal()][2]) {
            ((WorldInfo) world).getRemaining()[activationType.ordinal()]--;
            return activationRange[activationType.ordinal()][3];
        }
        return -1;
    }

    public enum ActivationType {
        VILLAGER(16, 4, 30 * 20, 5 * 20),
        WATER(16, 0, 60 * 20, 5 * 20),
        FLYING_MONSTER(32, 8, 10 * 20, 5 * 20),
        MONSTER(32, 8, 20 * 20, 5 * 20),
        ANIMAL(32, 4, 60 * 20, 5 * 20),
        RAIDER(48, 8, 20 * 20, 5 * 20),
        MISC(16, 0, 60 * 20, 5 * 20);

        private final int activationRange;
        private final int wakeUpInactiveMaxPerTick;
        private final int wakeUpInactiveEvery;
        private final int wakeUpInactiveFor;
        Box boundingBox = new Box(0, 0, 0, 0, 0, 0);

        ActivationType(int activationRange, int wakeUpInactiveMaxPerTick, int wakeUpInactiveEvery, int wakeUpInactiveFor) {
            this.activationRange = activationRange;
            this.wakeUpInactiveMaxPerTick = wakeUpInactiveMaxPerTick;
            this.wakeUpInactiveEvery = wakeUpInactiveEvery;
            this.wakeUpInactiveFor = wakeUpInactiveFor;
        }

        public int getActivationRange() {
            return activationRange;
        }

        public int getWakeUpInactiveMaxPerTick() {
            return wakeUpInactiveMaxPerTick;
        }

        public int getWakeUpInactiveEvery() {
            return wakeUpInactiveEvery;
        }

        public int getWakeUpInactiveFor() {
            return wakeUpInactiveFor;
        }
    }
}