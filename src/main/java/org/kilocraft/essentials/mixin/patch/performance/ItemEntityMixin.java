package org.kilocraft.essentials.mixin.patch.performance;

import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
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
            method = "merge(Lnet/minecraft/entity/ItemEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/ItemEntity;Lnet/minecraft/item/ItemStack;)V",
            at = @At(
                    value = "HEAD"
            )
    )
    private static void adjustMotion(ItemEntity itemEntity, ItemStack itemStack, ItemEntity itemEntity2, ItemStack itemStack2, CallbackInfo ci) {
        if (itemEntity.getVelocity().lengthSquared() < itemEntity2.getVelocity().lengthSquared() && ServerSettings.patch_item_merge_adjust_movement) {
            itemEntity.setVelocity(itemEntity2.getVelocity());
        }
    }

}
