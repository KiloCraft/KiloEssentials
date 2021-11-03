package org.kilocraft.essentials.mixin.patch.gameplay;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.EnchantCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EnchantCommand.class)
public abstract class EnchantCommandMixin {

    @Redirect(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/enchantment/Enchantment;getMaxLevel()I"
            )
    )
    private static int noMaxLevel(Enchantment enchantment) {
        return Integer.MAX_VALUE;
    }


    @Redirect(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/enchantment/Enchantment;isAcceptableItem(Lnet/minecraft/item/ItemStack;)Z"
            )
    )
    private static boolean alwaysCompatible(Enchantment enchantment, ItemStack itemStack) {
        return true;
    }

}
