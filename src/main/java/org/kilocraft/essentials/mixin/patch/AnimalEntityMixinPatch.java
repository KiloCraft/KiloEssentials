package org.kilocraft.essentials.mixin.patch;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.util.settings.ServerSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AnimalEntity.class)
public abstract class AnimalEntityMixinPatch extends PassiveEntity {

    protected AnimalEntityMixinPatch(EntityType<? extends PassiveEntity> entityType, World world) {
        super(entityType, world);
    }

    @Redirect(method = "interactMob", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getStackInHand(Lnet/minecraft/util/Hand;)Lnet/minecraft/item/ItemStack;"))
    public ItemStack allowBreeding(PlayerEntity playerEntity, Hand hand) {
        int range = ServerSettings.getInt("entity_limit.animals.range");
        int limit = ServerSettings.getInt("entity_limit.animals.limit");
        if (range !=-1 && limit !=-1) {
            if (limit <= this.getEntityWorld().getEntitiesByType(this.getType(), new Box(this.getBlockPos().mutableCopy().add(range, range, range), this.getBlockPos().mutableCopy().add(-range, -range, -range)), EntityPredicates.EXCEPT_SPECTATOR).size()) {
                OnlineUser user = KiloServer.getServer().getOnlineUser((ServerPlayerEntity) playerEntity);
                user.sendLangMessage("entity_limit.animal", limit, Registry.ENTITY_TYPE.getId(this.getType()).getPath(), range);
                return ItemStack.EMPTY;
            }
        }
        return playerEntity.getStackInHand(hand);
    }

}
