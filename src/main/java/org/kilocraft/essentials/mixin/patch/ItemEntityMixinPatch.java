package org.kilocraft.essentials.mixin.patch;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.kilocraft.essentials.util.settings.ServerSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixinPatch extends Entity {

    public ItemEntityMixinPatch(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    @Redirect(method = "tryMerge()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;getBoundingBox()Lnet/minecraft/util/math/Box;"))
    public Box adjustMergeRadius(ItemEntity itemEntity) {
        return this.getBoundingBox().expand(ServerSettings.patch_item_merge_radius, 0.0D, ServerSettings.patch_item_merge_radius);
    }

    @Inject(method = "merge(Lnet/minecraft/entity/ItemEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/ItemEntity;Lnet/minecraft/item/ItemStack;)V", at = @At(value = "INVOKE"))
    private static void adjustMotion(ItemEntity itemEntity, ItemStack itemStack, ItemEntity itemEntity2, ItemStack itemStack2, CallbackInfo ci) {
        if (itemEntity.getVelocity().lengthSquared() < itemEntity2.getVelocity().lengthSquared() && ServerSettings.patch_item_merge_adjust_movement) {
            itemEntity.setVelocity(itemEntity2.getVelocity());
        }
    }

}
