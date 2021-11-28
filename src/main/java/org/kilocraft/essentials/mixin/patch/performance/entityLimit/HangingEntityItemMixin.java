package org.kilocraft.essentials.mixin.patch.performance.entityLimit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HangingEntityItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.util.TickManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(HangingEntityItem.class)
public abstract class HangingEntityItemMixin extends Item {

    @Shadow
    protected abstract boolean mayPlace(Player player, Direction direction, ItemStack itemStack, BlockPos blockPos);

    public HangingEntityItemMixin(Properties settings) {
        super(settings);
    }

    @Redirect(
            method = "useOn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/HangingEntityItem;mayPlace(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/Direction;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/BlockPos;)Z"
            )
    )
    public boolean canUseItemFrame(HangingEntityItem decorationItem, Player playerEntity, Direction direction, ItemStack itemStack, BlockPos blockPos) {
        if (TickManager.isEntityLimitReached(playerEntity.getCommandSenderWorld(), blockPos, EntityType.ITEM_FRAME, EntityType.GLOW_ITEM_FRAME)) {
            OnlineUser user = KiloEssentials.getUserManager().getOnline((ServerPlayer) playerEntity);
            user.sendLangMessage("template.entity_limit", "item frame");
            return false;
        } else {
            return this.mayPlace(playerEntity, direction, itemStack, blockPos);
        }
    }
}
