package org.kilocraft.essentials.mixin;

import net.minecraft.class_4861;
import net.minecraft.container.BlockContext;
import net.minecraft.container.Container;
import net.minecraft.container.ContainerType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_4861.class)
public abstract class WorkbenchContainerMixin extends Container {

    @Shadow @Final protected BlockContext field_22481;

    @Shadow @Final protected Inventory field_22480;

    protected WorkbenchContainerMixin(ContainerType<?> containerType, int i) {
        super(containerType, i);
    }

    @Inject(
            method = "close",
            at = @At(value = "HEAD", target = "Lnet/minecraft/class_4861;close(Lnet/minecraft/entity/player/PlayerEntity;)V")
    )
    private void modify$close(PlayerEntity playerEntity, CallbackInfo ci) {
        if (this.field_22481 == BlockContext.EMPTY) {
            super.close(playerEntity);
            this.dropInventory(playerEntity, playerEntity.getEntityWorld(), this.field_22480);
            ci.cancel();
        }
    }

}
