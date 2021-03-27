package org.kilocraft.essentials.patch.entityActivationRange;

import net.minecraft.SharedConstants;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.*;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.mixin.entityActivationRange.PersistentProjectileEntityAccessor;
import org.kilocraft.essentials.util.settings.ServerSettings;

public class ActivationRange {

    static Box maxBB = new Box(0, 0, 0, 0, 0, 0);

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
        } else if (entity instanceof HostileEntity || entity instanceof SlimeEntity) {
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
        if ((activationType == ActivationType.MISC && ServerSettings.activationRange[0] <= 0)
                || (activationType == ActivationType.RAIDER && ServerSettings.activationRange[1] <= 0)
                || (activationType == ActivationType.ANIMAL && ServerSettings.activationRange[2] <= 0)
                || (activationType == ActivationType.MONSTER && ServerSettings.activationRange[3] <= 0)
                || entity instanceof PlayerEntity
                || entity instanceof ProjectileEntity
                || entity instanceof EnderDragonEntity
                || entity instanceof EnderDragonPart
                || entity instanceof WitherEntity
                || entity instanceof GhastEntity
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
        final int miscActivationRange = ServerSettings.activationRange[0];
        final int raiderActivationRange = ServerSettings.activationRange[1];
        final int animalActivationRange = ServerSettings.activationRange[2];
        final int monsterActivationRange = ServerSettings.activationRange[3];

        int maxRange = Math.max(monsterActivationRange, animalActivationRange);
        maxRange = Math.max(maxRange, raiderActivationRange);
        maxRange = Math.max(maxRange, miscActivationRange);
        maxRange = Math.min((KiloEssentials.getServer().getMinecraftServer().getPlayerManager().getViewDistance() << 4) - 8, maxRange);

        for (PlayerEntity player : world.getPlayers()) {
            if (player.isSpectator()) continue;
            ((ActivationTypeEntity) player).setActivatedTick(KiloEssentials.getServer().getMinecraftServer().getTicks());
            maxBB = player.getBoundingBox().expand(maxRange, 256, maxRange);
            ActivationType.MISC.boundingBox = player.getBoundingBox().expand(miscActivationRange, 256, miscActivationRange);
            ActivationType.RAIDER.boundingBox = player.getBoundingBox().expand(raiderActivationRange, 256, raiderActivationRange);
            ActivationType.ANIMAL.boundingBox = player.getBoundingBox().expand(animalActivationRange, 256, animalActivationRange);
            ActivationType.MONSTER.boundingBox = player.getBoundingBox().expand(monsterActivationRange, 256, monsterActivationRange);

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
        if (KiloEssentials.getServer().getMinecraftServer().getTicks() > activationTypeEntity.getActivatedTick()) {
            if (activationTypeEntity.getDefaultActivationState()) {
                activationTypeEntity.setActivatedTick(KiloEssentials.getServer().getMinecraftServer().getTicks());
                return;
            }
            if (activationTypeEntity.getActivationType().boundingBox.intersects(entity.getBoundingBox())) {
                activationTypeEntity.setActivatedTick(KiloEssentials.getServer().getMinecraftServer().getTicks());
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
    public static boolean checkEntityImmunities(Entity entity) {
        // quick checks.
        if (entity.isSubmergedInWater() || entity.getFireTicks() > 0) {
            return true;
        }
        if (!(entity instanceof ArrowEntity)) {
            if (!entity.isOnGround() || !entity.getPassengerList().isEmpty()) {
                return true;
            }
        } else if (!((PersistentProjectileEntityAccessor) entity).isInGround()) {
            return true;
        }
        // special cases.
        if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) entity;
            if (living.hurtTime > 0 || living.getStatusEffects().size() > 0) {
                return true;
            }
            if (entity instanceof PathAwareEntity && ((PathAwareEntity) entity).getTarget() != null) {
                return true;
            }
            if (entity instanceof VillagerEntity && ((VillagerEntity) entity).canBreed()) {
                return true;
            }
            if (entity instanceof AnimalEntity) {
                AnimalEntity animal = (AnimalEntity) entity;
                if (animal.isBaby() || animal.isInLove()) {
                    return true;
                }
                if (entity instanceof SheepEntity && ((SheepEntity) entity).isSheared()) {
                    return true;
                }
            }
            if (entity instanceof CreeperEntity && ((CreeperEntity) entity).isIgnited()) { // isExplosive
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the entity is active for this tick.
     *
     * @param entity
     * @return
     */
    public static boolean checkIfActive(Entity entity) {

        ActivationTypeEntity activationTypeEntity = (ActivationTypeEntity) entity;
        boolean isActive = activationTypeEntity.getActivatedTick() >= KiloEssentials.getServer().getMinecraftServer().getTicks() || activationTypeEntity.getDefaultActivationState();

        // Should this entity tick?
        if (!isActive) {
            if ((KiloEssentials.getServer().getMinecraftServer().getTicks() - activationTypeEntity.getActivatedTick() - 1) % 20 == 0) {
                // Check immunities every 20 ticks.
                if (checkEntityImmunities(entity)) {
                    // Triggered some sort of immunity, give 20 full ticks before we check again.
                    activationTypeEntity.setActivatedTick(KiloEssentials.getServer().getMinecraftServer().getTicks() + 20);
                }
                isActive = true;
            }
            // Add a little performance juice to active entities. Skip 1/4 if not immune.
        } else if (!activationTypeEntity.getDefaultActivationState() && entity.age % 4 == 0 && !checkEntityImmunities(entity)) {
            isActive = false;
        }
        return isActive;
    }

    public enum ActivationType {
        MONSTER,
        ANIMAL,
        RAIDER,
        MISC;

        Box boundingBox = new Box(0, 0, 0, 0, 0, 0);
    }
}