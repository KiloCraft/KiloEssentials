package org.kilocraft.essentials.mixin.patch.technical;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.util.CommandPermission;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CraftingMenu.class)
public abstract class CraftingMenuMixin {

    @Inject(
            method = "stillValid",
            at = @At("HEAD"),
            cancellable = true
    )
    public void allowUsage(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (KiloEssentials.hasPermissionNode(player.createCommandSourceStack(), CommandPermission.WORKBENCH)) cir.setReturnValue(true);
    }

}
