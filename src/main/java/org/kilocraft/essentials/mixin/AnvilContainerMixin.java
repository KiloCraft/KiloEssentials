package org.kilocraft.essentials.mixin;

import net.minecraft.container.AnvilContainer;
import net.minecraft.container.BlockContext;
import net.minecraft.container.Container;
import net.minecraft.container.ContainerType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import org.apache.commons.lang3.StringUtils;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilContainer.class)
public abstract class AnvilContainerMixin extends Container {

    @Shadow
    private String newItemName;

    protected AnvilContainerMixin(ContainerType<?> containerType, int i) {
        super(containerType, i);
    }

    @Shadow
    public void updateResult(){};
    @Final
    @Shadow private PlayerEntity player;

    @Shadow @Final private BlockContext context;

    @Shadow @Final private Inventory inventory;

    @Inject(method = "close", cancellable = true, at = @At(value = "HEAD", target = "Lnet/minecraft/container/AnvilContainer;close(Lnet/minecraft/entity/player/PlayerEntity;)V"))
    public void modifyClose(PlayerEntity playerEntity, CallbackInfo ci) {
        if (this.context == BlockContext.EMPTY) {
            super.close(playerEntity);
            this.dropInventory(playerEntity, playerEntity.getEntityWorld(), this.inventory);
            ci.cancel();
        }
    }

    @Inject(method = "setNewItemName", cancellable = true, at = @At(value = "HEAD", target = "Lnet/minecraft/container/AnvilContainer;setNewItemName(Ljava/lang/String;)V\n"))
    public void modifySetNewItemName(String string, CallbackInfo ci) {
        ci.cancel();

        newItemName = TextFormat.translate(string,
                KiloCommands.hasPermission(player.getCommandSource(), CommandPermission.ITEM_NAME));

        if (((AnvilContainer)(Object)this).getSlot(2).hasStack()) {
            ItemStack itemStack = ((AnvilContainer)(Object)this).getSlot(2).getStack();
            if (StringUtils.isBlank(string)) {
                itemStack.removeCustomName();
            } else {
                itemStack.setCustomName(new LiteralText(newItemName));
            }
        }

        updateResult();
    }
}
