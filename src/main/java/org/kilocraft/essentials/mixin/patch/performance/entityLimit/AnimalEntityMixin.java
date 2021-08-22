package org.kilocraft.essentials.mixin.patch.performance.entityLimit;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.util.TickManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AnimalEntity.class)
public abstract class AnimalEntityMixin extends PassiveEntity {

    protected AnimalEntityMixin(EntityType<? extends PassiveEntity> entityType, World world) {
        super(entityType, world);
    }

    @Redirect(method = "interactMob", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getStackInHand(Lnet/minecraft/util/Hand;)Lnet/minecraft/item/ItemStack;"))
    public ItemStack allowBreeding(PlayerEntity playerEntity, Hand hand) {
        if (TickManager.isEntityLimitReached(this.getEntityWorld(), this.getBlockPos(), this.getType())) {
            OnlineUser user = KiloEssentials.getUserManager().getOnline((ServerPlayerEntity) playerEntity);
            user.sendLangMessage("template.entity_limit", Registry.ENTITY_TYPE.getId(this.getType()).getPath());
            return ItemStack.EMPTY;
        } else {
            return playerEntity.getStackInHand(hand);
        }
    }

}
