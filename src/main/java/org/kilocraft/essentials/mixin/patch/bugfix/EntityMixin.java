package org.kilocraft.essentials.mixin.patch.bugfix;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @ModifyArg(
            method = "spawnAtLocation(Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/item/ItemEntity;<init>(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/item/ItemStack;)V"
            ),
            index = 4
    )
    public ItemStack createNewItemStack(ItemStack original) {
        // Clone so we can destroy original
        return original.copy();
    }

    @Inject(
            method = "spawnAtLocation(Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/item/ItemEntity;setDefaultPickUpDelay()V"
            )
    )
    public void deleteOriginalItemStack(ItemStack stack, float yOffset, CallbackInfoReturnable<ItemEntity> cir) {
        // Destroy this item - if this ever leaks due to game bugs, ensure it doesn't dupe
        stack.setCount(0);
    }

}
