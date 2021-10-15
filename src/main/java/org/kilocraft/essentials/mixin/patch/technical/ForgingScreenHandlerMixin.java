package org.kilocraft.essentials.mixin.patch.technical;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ForgingScreenHandler;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.util.CommandPermission;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ForgingScreenHandler.class)
public abstract class ForgingScreenHandlerMixin {

    @Inject(
            method = "canUse(Lnet/minecraft/entity/player/PlayerEntity;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    public void allowUsage(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if (KiloEssentials.hasPermissionNode(player.getCommandSource(), CommandPermission.ANVIL) && (ForgingScreenHandler) (Object) this instanceof AnvilScreenHandler)
            cir.setReturnValue(true);
    }

}
