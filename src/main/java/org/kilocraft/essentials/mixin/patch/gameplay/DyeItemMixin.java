package org.kilocraft.essentials.mixin.patch.gameplay;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import org.kilocraft.essentials.mixin.accessor.ShulkerAccessor;
import org.kilocraft.essentials.util.settings.ServerSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DyeItem.class)
public abstract class DyeItemMixin {

    @Shadow
    @Final
    private DyeColor dyeColor;

    // Allows players to use dye on shulker entities
    @Inject(
            method = "interactLivingEntity",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void dyeShulkerEntity(ItemStack stack, Player user, LivingEntity entity, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (entity instanceof Shulker shulkerEntity && ServerSettings.getBoolean("patch.dye_shulkers")) {
            if (shulkerEntity.isAlive() && shulkerEntity.getColor() != this.dyeColor) {
                if (!user.level.isClientSide) {
                    ((ShulkerAccessor) shulkerEntity).setColor(this.dyeColor);
                    user.swing(InteractionHand.MAIN_HAND, true);
                    stack.shrink(1);
                }
                cir.setReturnValue(InteractionResult.sidedSuccess(user.level.isClientSide));
            }
        }
    }

}
