package org.kilocraft.essentials.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ForgingScreenHandler.class)
public abstract class ForgingScreenHandlerMixin extends ScreenHandler {

    @Shadow @Final protected PlayerEntity player;

    @Shadow @Final protected Inventory input;

    @Shadow @Final protected ScreenHandlerContext context;

    protected ForgingScreenHandlerMixin(ScreenHandlerType<?> screenHandlerType, int i) {
        super(screenHandlerType, i);
    }

    @Inject(
            method = "close",
            at = @At(value = "HEAD", target = "Lnet/minecraft/screen/ForgingScreenHandler;close(Lnet/minecraft/entity/player/PlayerEntity;)V"),
            cancellable = true
    )
    private void modify$close(PlayerEntity playerEntity, CallbackInfo ci) {
        if (this.context == ScreenHandlerContext.EMPTY) {
            super.close(playerEntity);
            this.dropInventory(playerEntity, this.input);
            ci.cancel();
        }
    }

}
