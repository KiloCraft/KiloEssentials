package org.kilocraft.essentials.api;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.*;
import net.minecraft.entity.projectile.thrown.*;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.util.EntityServerRayTraceable;

public class ThrowableEntities {
    public static Entity create(Type type, World world, PlayerEntity player, float yaw, float pitch) {
        return create(type, world, player, yaw, pitch, 0.0F, 1.5F, 1.0F);
    }

    public static Entity create(
            Type type, World world, PlayerEntity player, float yaw, float pitch, float motionX, float motionY, float motionZ
    ) {
        switch (type) {
            case EGG:
                return completeThrownEntity(new EggEntity(world, player), player, yaw, pitch, motionX, motionY, motionZ);
            case SNOWBALL:
                return completeThrownEntity(new SnowballEntity(world, player), player, yaw, pitch, motionX, motionY, motionZ);
            case ENDER_PEARL:
                return completeThrownEntity(new EnderPearlEntity(world, player), player, yaw, pitch, motionX, motionY, motionZ);
            case EXPERIENCE_BOTTLE:
                return completeThrownEntity(new ExperienceBottleEntity(world, player), player, yaw, pitch, motionX, motionY, motionZ);
            case POTION:
                return completeThrown(new PotionEntity(world, player), player, yaw, pitch, motionX, motionY, motionZ);
            case ARROW:
                return completeProjectile(new ArrowEntity(world, player), player, yaw, pitch, motionX, motionY, motionZ);
            case DRAGON_FIRE_BALL:
                return completeAbstracted(EntityType.DRAGON_FIREBALL, world, player);
            case FIREBALL:
                Vec3d pos = rayTrace(player).getPos();
                return new FireballEntity(world, player, pos.getX(), pos.getY(), pos.getZ());
            case LLAMA_SPIT:
                LlamaSpitEntity spitEntity = (LlamaSpitEntity) completeAbstracted(EntityType.LLAMA_SPIT, world, player);
                Vec3d vec3d = rayTrace(player, 15.0D).getPos();
                double d = vec3d.getX() - player.getX();
                double e = player.getBodyY(0.3333333333333333D) - spitEntity.getY();
                double f = vec3d.getZ() - player.getZ();
                float g = MathHelper.sqrt(d * d + f * f) * 0.2F;
                spitEntity.setVelocity(d, e + g, f, 1.5F, 10.0F);
                return spitEntity;
            case SHULKER_BULLET:
                return completeAbstracted(EntityType.SHULKER_BULLET, world, player);
            case SMALL_FIREBALL:
                return completeAbstracted(EntityType.SMALL_FIREBALL, world, player);
            case SPECTRAL_ARROW:
                return completeProjectile(new SpectralArrowEntity(world, player), player, pitch, yaw, motionX, motionY, motionZ);
            case TRIDENT:
                return completeAbstracted(EntityType.TRIDENT, world, player);
            case WITHER_SKULL:
                return completeAbstracted(EntityType.WITHER_SKULL, world, player);
            case FISHING_BOBBER:
                return completeAbstracted(EntityType.FISHING_BOBBER, world, player);
        }

        return null;
    }

    public static ThrownItemEntity completeThrownEntity(ThrownItemEntity entity, PlayerEntity player, float yaw, float pitch, float motionX, float motionY, float motionZ) {
        //Yarn (19.w.09.a): entity.setProperties(player, pitch, yaw, motionX, motionY, motionZ);
        entity.setOwner(player);
        entity.setVelocity(motionX, motionY, motionZ, yaw, pitch);
        return entity;
    }

    public static ProjectileEntity completeProjectile(ProjectileEntity entity, PlayerEntity player, float yaw, float pitch, float motionX, float motionY, float motionZ) {
        //Yarn (19.w.09.a): entity.setProperties(player, pitch, yaw, motionX, motionY, motionZ);
        entity.setOwner(player);
        entity.setVelocity(motionX, motionY, motionZ, yaw, pitch);
        return entity;
    }

    public static ThrownEntity completeThrown(ThrownEntity entity, PlayerEntity player, float yaw, float pitch, float motionX, float motionY, float motionZ) {
        //Yarn (19.w.09.a): entity.setProperties(player, pitch, yaw, motionX, motionY, motionZ);
        entity.setOwner(player);
        entity.setVelocity(motionX, motionY, motionZ, yaw, pitch);
        return entity;
    }

    public static Entity completeAbstracted(EntityType<?> type, World world, PlayerEntity player) {
        Entity entity = type.create(world, null, null, player, player.getBlockPos(), SpawnType.COMMAND, true, false);
        if (entity == null)
            return null;

        updateMotionFor(entity, player);
        return entity;
    }

    public static void updateMotionFor(Entity entity, PlayerEntity player) {
        entity.updatePosition(
                player.getX() - (double)(player.getWidth() + 1.0F) * 0.5D * (double) MathHelper.sin(player.bodyYaw * 0.017453292F),
                player.getEyeY() - 0.10000000149011612D,
                player.getZ() + (double)(player.getWidth() + 1.0F) * 0.5D * (double)MathHelper.cos(player.bodyYaw * 0.017453292F));
    }

    public static HitResult rayTrace(PlayerEntity player) {
        return ((EntityServerRayTraceable) player).rayTrace(80.0D, 1.0F, true);
    }

    public static HitResult rayTrace(PlayerEntity player, double maxDistance) {
        return ((EntityServerRayTraceable) player).rayTrace(maxDistance, 1.0F, true);
    }

    public enum Type {
        EGG("egg"),
        SNOWBALL("snowball"),
        ENDER_PEARL("ender_pearl"),
        EXPERIENCE_BOTTLE("experience_bottle"),
        POTION("potion"),
        ARROW("arrow"),
        DRAGON_FIRE_BALL("dragon_fire_ball"),
        FIREBALL("fireball"),
        LLAMA_SPIT("llama_spit"),
        SHULKER_BULLET("shulker_bullet"),
        SMALL_FIREBALL("small_fireball"),
        SPECTRAL_ARROW("spectral_arrow"),
        TRIDENT("trident"),
        WITHER_SKULL("wither_skull"),
        FISHING_BOBBER("fishing_bobber");

        private String name;
        Type(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Nullable
        public static ThrowableEntities.Type getByName(String name) {
            for (Type value : values()) {
                if (value.name.equals(name))
                    return value;
            }

            return null;
        }
    }
}
