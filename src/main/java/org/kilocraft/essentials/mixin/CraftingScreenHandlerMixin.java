package org.kilocraft.essentials.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.*;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingScreenHandler.class)
public abstract class CraftingScreenHandlerMixin extends AbstractRecipeScreenHandler<CraftingInventory> {

    public CraftingScreenHandlerMixin(ScreenHandlerType<?> screenHandlerType, int i) {
        super(screenHandlerType, i);
    }

    @Shadow @Final private PlayerEntity player;

    @Shadow @Final private CraftingInventory input;

    @Shadow @Final private CraftingResultInventory result;

    @Shadow @Final private ScreenHandlerContext context;

    @Shadow
    protected static void updateResult(ScreenHandler screenHandler, World world, PlayerEntity playerEntity, CraftingInventory craftingInventory, CraftingResultInventory craftingResultInventory) {
    }

    @Inject(method = "onContentChanged", cancellable = true, at = @At(value = "HEAD", target = "Lnet/minecraft/screen/CraftingScreenHandler;onContentChanged(Lnet/minecraft/inventory/Inventory;)V"))
    public void modifyOnContentChanged(Inventory inventory, CallbackInfo ci) {
        if (this.context == ScreenHandlerContext.EMPTY) {
            updateResult(this, this.player.getEntityWorld(), this.player, this.input, this.result);
            ci.cancel();
        }
    }

    @Inject(method = "close", cancellable = true, at = @At(value = "HEAD", target = "Lnet/minecraft/screen/CraftingScreenHandler;close(Lnet/minecraft/entity/player/PlayerEntity;)V"))
    public void modifyClose(PlayerEntity playerEntity, CallbackInfo ci) {
        if (this.context == ScreenHandlerContext.EMPTY) {
            super.close(playerEntity);
            this.dropInventory(playerEntity, this.input);
            ci.cancel();
        }
    }

}
