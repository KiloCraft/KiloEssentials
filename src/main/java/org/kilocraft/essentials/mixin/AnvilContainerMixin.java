package org.kilocraft.essentials.mixin;

import net.minecraft.class_4861;
import net.minecraft.container.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import org.apache.commons.lang3.StringUtils;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.text.TextFormat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilContainer.class)
public abstract class AnvilContainerMixin extends class_4861 {

    @Shadow
    private String newItemName;

    @Shadow public abstract void method_24928();

    public AnvilContainerMixin(int i, PlayerInventory playerInventory, BlockContext blockContext) {
        super(ContainerType.ANVIL, i, playerInventory, blockContext);
    }

//    @Shadow
//    public void updateResult(){};
//    @Final
//    @Shadow private PlayerEntity player;
//
//    @Shadow @Final private BlockContext context;
//
//    @Shadow @Final private Inventory inventory;

//    @Inject(method = "close", cancellable = true, at = @At(value = "HEAD", target = "Lnet/minecraft/container/AnvilContainer;close(Lnet/minecraft/entity/player/PlayerEntity;)V"))
//    public void modifyClose(PlayerEntity playerEntity, CallbackInfo ci) {
//        if (this.context == BlockContext.EMPTY) {
//            super.close(playerEntity);
//            this.dropInventory(playerEntity, playerEntity.getEntityWorld(), this.inventory);
//            ci.cancel();
//        }
//    }

    @Inject(method = "setNewItemName", cancellable = true, at = @At(value = "HEAD", target = "Lnet/minecraft/container/AnvilContainer;setNewItemName(Ljava/lang/String;)V\n"))
    public void modifySetNewItemName(String string, CallbackInfo ci) {
        ci.cancel();
        newItemName = TextFormat.translate(string,
                KiloCommands.hasPermission(super.field_22482.getCommandSource(), CommandPermission.ITEM_NAME));

        if (((AnvilContainer)(Object)this).getSlot(2).hasStack()) {
            ItemStack itemStack = ((AnvilContainer)(Object)this).getSlot(2).getStack();
            if (StringUtils.isBlank(string)) {
                itemStack.removeCustomName();
            } else {
                itemStack.setCustomName(new LiteralText(newItemName));
            }
        }

        method_24928();
    }
}
