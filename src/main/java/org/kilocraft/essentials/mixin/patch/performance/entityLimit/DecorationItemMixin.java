package org.kilocraft.essentials.mixin.patch.performance.entityLimit;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DecorationItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.util.TickManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DecorationItem.class)
public abstract class DecorationItemMixin extends Item {

    @Shadow
    protected abstract boolean canPlaceOn(PlayerEntity playerEntity, Direction direction, ItemStack itemStack, BlockPos blockPos);

    public DecorationItemMixin(Settings settings) {
        super(settings);
    }

    @Redirect(method = "useOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/DecorationItem;canPlaceOn(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/Direction;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/math/BlockPos;)Z"))
    public boolean canUseItemFrame(DecorationItem decorationItem, PlayerEntity playerEntity, Direction direction, ItemStack itemStack, BlockPos blockPos) {
        if (TickManager.isEntityLimitReached(playerEntity.getEntityWorld(), blockPos, EntityType.ITEM_FRAME, EntityType.GLOW_ITEM_FRAME)) {
            OnlineUser user = KiloEssentials.getUserManager().getOnline((ServerPlayerEntity) playerEntity);
            user.sendLangMessage("template.entity_limit", "item frame");
            return false;
        } else {
            return this.canPlaceOn(playerEntity, direction, itemStack, blockPos);
        }
    }
}
