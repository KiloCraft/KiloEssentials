package org.kilocraft.essentials.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.BlockContext;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.CraftingTableScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingTableScreenHandler.class)
public abstract class CraftingTableScreenHandlerMixin extends CraftingScreenHandler<CraftingInventory> {


    public CraftingTableScreenHandlerMixin(ScreenHandlerType<?> screenHandlerType, int i) {
        super(screenHandlerType, i);
    }

    @Shadow
    protected static void updateResult(int i, World world, PlayerEntity playerEntity, CraftingInventory craftingInventory, CraftingResultInventory craftingResultInventory) {
    }

    @Shadow @Final private PlayerEntity player;

    @Shadow @Final private CraftingInventory craftingInv;

    @Shadow @Final private CraftingResultInventory resultInv;

    @Shadow @Final private BlockContext context;

    @Inject(method = "onContentChanged", cancellable = true, at = @At(value = "HEAD", target = "Lnet/minecraft/container/CraftingTableContainer;onContentChanged(Lnet/minecraft/inventory/Inventory;)V"))
    public void modifyOnContentChanged(Inventory inventory, CallbackInfo ci) {
        if (this.context == BlockContext.EMPTY) {
            updateResult(this.syncId, this.player.getEntityWorld(), this.player, this.craftingInv, this.resultInv);
            ci.cancel();
        }
    }

    @Inject(method = "close", cancellable = true, at = @At(value = "HEAD", target = "Lnet/minecraft/container/CraftingTableContainer;close(Lnet/minecraft/entity/player/PlayerEntity;)V"))
    public void modifyClose(PlayerEntity playerEntity, CallbackInfo ci) {
        if (this.context == BlockContext.EMPTY) {
            super.close(playerEntity);
            this.dropInventory(playerEntity, playerEntity.getEntityWorld(), this.craftingInv);
            ci.cancel();
        }
    }

}
