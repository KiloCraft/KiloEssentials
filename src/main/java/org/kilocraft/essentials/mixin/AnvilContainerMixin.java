package org.kilocraft.essentials.mixin;

import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.container.AnvilContainer;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import org.apache.commons.lang3.StringUtils;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;

@Mixin(AnvilContainer.class)
public abstract class AnvilContainerMixin {

    @Shadow
    private String newItemName;
    @Shadow
    public void updateResult(){};
    @Final
    @Shadow private PlayerEntity player;

    public void close(PlayerEntity playerEntity) {
        if (((AnvilContainer)(Object)this).getSlot(0).hasStack()) {
            ItemStack itemStack = ((AnvilContainer)(Object)this).getSlot(0).getStack();
            ItemEntity entity = player.dropItem(itemStack, true);
            Objects.requireNonNull(entity).setPickupDelay(0);
        }
    }

    public void setNewItemName(String string) {
        newItemName = string;

        if (Thimble.PERMISSIONS.hasPermission(EssentialPermission.ANVIL_COLOR.getNode(), player.getGameProfile().getId()) || player.allowsPermissionLevel(2)) {
            newItemName = TextFormat.translateAlternateColorCodes('&', string);
        } else {
            newItemName = TextFormat.removeAlternateColorCodes('&', string);
        }

        if (((AnvilContainer)(Object)this).getSlot(2).hasStack()) {
            ItemStack itemStack = ((AnvilContainer)(Object)this).getSlot(2).getStack();
            if (StringUtils.isBlank(string)) {
                itemStack.removeCustomName();
            } else {
                itemStack.setCustomName(new LiteralText( newItemName));
            }
        }

        updateResult();
    }
}
