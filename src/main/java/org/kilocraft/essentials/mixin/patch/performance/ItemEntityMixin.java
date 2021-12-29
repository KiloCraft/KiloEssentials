package org.kilocraft.essentials.mixin.patch.performance;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.kilocraft.essentials.util.settings.ServerSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {

    /**
     * Adjust item movement on merge
     */
    @Inject(
            method = "merge(Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At(
                    value = "HEAD"
            )
    )
    private static void adjustMotion(ItemEntity itemEntity, ItemStack itemStack, ItemEntity itemEntity2, ItemStack itemStack2, CallbackInfo ci) {
        if (itemEntity.getDeltaMovement().lengthSqr() < itemEntity2.getDeltaMovement().lengthSqr() && ServerSettings.patch_item_merge_adjust_movement) {
            itemEntity.setDeltaMovement(itemEntity2.getDeltaMovement());
        }
    }

}
