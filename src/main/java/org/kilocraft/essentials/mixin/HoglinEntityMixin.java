package org.kilocraft.essentials.mixin;

import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.HoglinEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.kilocraft.essentials.api.KiloEssentials;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(HoglinEntity.class)
public abstract class HoglinEntityMixin extends PassiveEntity {

    protected HoglinEntityMixin(EntityType<? extends PassiveEntity> entityType, World world) {
        super(entityType, world);
    }

    @Redirect(method = "initialize", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/AnimalEntity;initialize(Lnet/minecraft/world/ServerWorldAccess;Lnet/minecraft/world/LocalDifficulty;Lnet/minecraft/entity/SpawnReason;Lnet/minecraft/entity/EntityData;Lnet/minecraft/nbt/NbtCompound;)Lnet/minecraft/entity/EntityData;"))
    public EntityData onInitialize(AnimalEntity animalEntity, ServerWorldAccess serverWorldAccess, LocalDifficulty localDifficulty, SpawnReason spawnReason, EntityData entityData, NbtCompound NbtCompound) {
        if (entityData != null && !(entityData instanceof HoglinEntity) && !(entityData instanceof PassiveData)) {
            KiloEssentials.getLogger().warn("There was an error initializing " + this.toString());
            KiloEssentials.getLogger().warn("World: " + serverWorldAccess.toServerWorld().toString());
            KiloEssentials.getLogger().warn("Spawnreason: " + spawnReason.toString());
            KiloEssentials.getLogger().warn("EntityData: " + entityData);
            KiloEssentials.getLogger().warn("NbtCompound: " + NbtCompound);
            entityData = null;
        }
        return super.initialize(serverWorldAccess, localDifficulty, spawnReason, entityData, NbtCompound);
    }

}
