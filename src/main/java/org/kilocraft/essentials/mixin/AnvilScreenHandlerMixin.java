package org.kilocraft.essentials.mixin;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.BlockContext;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
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

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin extends ForgingScreenHandler {

    @Shadow
    private String newItemName;

    @Shadow public abstract void updateResult();

    public AnvilScreenHandlerMixin(ScreenHandlerType<?> screenHandlerType, int i, PlayerInventory playerInventory, BlockContext blockContext) {
        super(screenHandlerType, i, playerInventory, blockContext);
    }


    @Inject(method = "setNewItemName", cancellable = true, at = @At(value = "HEAD", target = "Lnet/minecraft/container/AnvilContainer;setNewItemName(Ljava/lang/String;)V\n"))
    public void modifySetNewItemName(String string, CallbackInfo ci) {
        ci.cancel();
        newItemName = TextFormat.translate(string,
                KiloCommands.hasPermission(super.player.getCommandSource(), CommandPermission.ITEM_NAME));

        if (((AnvilScreenHandler)(Object)this).getSlot(2).hasStack()) {
            ItemStack itemStack = ((AnvilScreenHandler)(Object)this).getSlot(2).getStack();
            if (StringUtils.isBlank(string)) {
                itemStack.removeCustomName();
            } else {
                itemStack.setCustomName(new LiteralText(newItemName));
            }
        }

        this.updateResult();
    }
}
