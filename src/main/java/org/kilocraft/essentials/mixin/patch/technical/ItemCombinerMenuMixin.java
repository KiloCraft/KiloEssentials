package org.kilocraft.essentials.mixin.patch.technical;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.util.CommandPermission;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemCombinerMenu.class)
public abstract class ItemCombinerMenuMixin extends AbstractContainerMenu {

    protected ItemCombinerMenuMixin(@Nullable MenuType<?> screenHandlerType, int i) {
        super(screenHandlerType, i);
    }

    @Inject(
            method = "stillValid",
            at = @At("HEAD"),
            cancellable = true
    )
    public void allowUsage(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (KiloEssentials.hasPermissionNode(player.createCommandSourceStack(), CommandPermission.ANVIL) && ((AbstractContainerMenu) this) instanceof AnvilMenu) {
            cir.setReturnValue(true);
        }
    }

}
