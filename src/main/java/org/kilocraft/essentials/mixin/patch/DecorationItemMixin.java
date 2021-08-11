package org.kilocraft.essentials.mixin.patch;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DecorationItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.util.settings.ServerSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DecorationItem.class)
public abstract class DecorationItemMixin extends Item {

    @Shadow protected abstract boolean canPlaceOn(PlayerEntity playerEntity, Direction direction, ItemStack itemStack, BlockPos blockPos);

    public DecorationItemMixin(Settings settings) {
        super(settings);
    }

    @Redirect(method = "useOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/DecorationItem;canPlaceOn(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/Direction;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/math/BlockPos;)Z"))
    public boolean canUseItemFrame(DecorationItem decorationItem, PlayerEntity playerEntity, Direction direction, ItemStack itemStack, BlockPos blockPos) {
        int range = ServerSettings.getInt("entity_limit." + Registry.ENTITY_TYPE.getId(EntityType.ITEM_FRAME).getPath() + ".range");
        int limit = ServerSettings.getInt("entity_limit." + Registry.ENTITY_TYPE.getId(EntityType.ITEM_FRAME).getPath() + ".limit");
        if (range !=-1 && limit !=-1) {
            Box box = new Box(blockPos.mutableCopy().add(range, range, range), blockPos.mutableCopy().add(-range, -range, -range));
            if (limit <= playerEntity.getEntityWorld().getEntitiesByType(EntityType.ITEM_FRAME, box, EntityPredicates.EXCEPT_SPECTATOR).size() + playerEntity.getEntityWorld().getEntitiesByType(EntityType.GLOW_ITEM_FRAME, box, EntityPredicates.EXCEPT_SPECTATOR).size()) {
                OnlineUser user = KiloEssentials.getUserManager().getOnline((ServerPlayerEntity) playerEntity);
                user.sendLangMessage("template.entity_limit", limit, "item frame", range);
                return false;
            }
        }
        return this.canPlaceOn(playerEntity, direction, itemStack, blockPos);
    }
}
