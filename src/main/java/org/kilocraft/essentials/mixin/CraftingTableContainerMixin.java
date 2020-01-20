package org.kilocraft.essentials.mixin;

import net.minecraft.container.BlockContext;
import net.minecraft.container.ContainerType;
import net.minecraft.container.CraftingContainer;
import net.minecraft.container.CraftingTableContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingTableContainer.class)
public abstract class CraftingTableContainerMixin extends CraftingContainer<CraftingInventory> {

    @Shadow @Final private BlockContext context;

    public CraftingTableContainerMixin(ContainerType<?> containerType, int i) {
        super(containerType, i);
    }

    @Shadow
    protected static void updateResult(int i, World world, PlayerEntity playerEntity, CraftingInventory craftingInventory, CraftingResultInventory craftingResultInventory) {
    }

    @Shadow @Final private PlayerEntity player;

    @Shadow @Final private CraftingInventory craftingInv;

    @Shadow @Final private CraftingResultInventory resultInv;

    @Inject(method = "onContentChanged", cancellable = true, at = @At(value = "HEAD", target = "Lnet/minecraft/container/CraftingTableContainer;onContentChanged(Lnet/minecraft/inventory/Inventory;)V"))
    public void modifyOnContentChanged(Inventory inventory, CallbackInfo ci) {
        if (this.context == BlockContext.EMPTY) {
            updateResult(this.syncId, this.player.getEntityWorld(), this.player, this.craftingInv, this.resultInv);
            ci.cancel();
        }
    }

    @Inject(method = "close", cancellable = true, at = @At(value = "HEAD", target = "Lnet/minecraft/container/CraftingTableContainer;close(Lnet/minecraft/entity/player/PlayerEntity;)V"))
    public void modifyClose(PlayerEntity playerEntity, CallbackInfo ci) {

    }

}
