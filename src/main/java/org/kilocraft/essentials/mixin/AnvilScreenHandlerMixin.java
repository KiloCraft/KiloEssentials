package org.kilocraft.essentials.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.SharedConstants;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.LiteralText;
import org.apache.commons.lang3.StringUtils;
import org.kilocraft.essentials.Format;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.util.PermissionUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin extends ForgingScreenHandler {

    @Shadow
    private String newItemName;

    @Shadow public abstract void updateResult();

    @Shadow private int repairItemUsage;

    @Shadow @Final private Property levelCost;

    public AnvilScreenHandlerMixin(ScreenHandlerType<?> screenHandlerType, int i, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(screenHandlerType, i, playerInventory, context);
    }

    @Inject(method = "setNewItemName", cancellable = true, at = @At(value = "HEAD", target = "Lnet/minecraft/screen/AnvilScreenHandler;setNewItemName(Ljava/lang/String;)V"))
    public void modifySetNewItemName(String string, CallbackInfo ci) throws CommandSyntaxException {
        ci.cancel();

        newItemName = Format.validatePermission(KiloEssentials.getServer().getOnlineUser((ServerPlayerEntity) player), string, PermissionUtil.COMMAND_PERMISSION_PREFIX + "item.formatting.");
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


    /**
     * @author Drex
     */
    @Overwrite
    protected void onTakeOutput(PlayerEntity player, ItemStack stack) {
        if (!player.getAbilities().creativeMode) {
            player.addExperienceLevels(-this.levelCost.get());
        }

        this.input.setStack(0, ItemStack.EMPTY);
        if (this.repairItemUsage > 0) {
            ItemStack itemStack = this.input.getStack(1);
            if (!itemStack.isEmpty() && itemStack.getCount() > this.repairItemUsage) {
                itemStack.decrement(this.repairItemUsage);
                this.input.setStack(1, itemStack);
            } else {
                this.input.setStack(1, ItemStack.EMPTY);
            }
        } else {
            this.input.setStack(1, ItemStack.EMPTY);



        }

        this.levelCost.set(0);
        this.context.run((world, blockPos) -> {
            BlockState blockState = world.getBlockState(blockPos);
            if (!player.getAbilities().creativeMode && blockState.isIn(BlockTags.ANVIL) && player.getRandom().nextFloat() < 0.12F) {
                BlockState blockState2 = AnvilBlock.getLandingState(blockState);
                if (blockState2 == null) {
                    world.removeBlock(blockPos, false);
                    world.syncWorldEvent(1029, blockPos, 0);
                } else {
                    world.setBlockState(blockPos, blockState2, 2);
                    world.syncWorldEvent(1030, blockPos, 0);
                }
            } else {
                world.syncWorldEvent(1030, blockPos, 0);
            }
        });
    }


}
