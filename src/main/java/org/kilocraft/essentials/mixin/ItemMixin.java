package org.kilocraft.essentials.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.player.PlayerUseItemEvent;
import org.kilocraft.essentials.events.player.PlayerUseItemEventImpl;
import org.kilocraft.essentials.util.ItemNbtUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class ItemMixin {

    @Inject(at = @At(value = "HEAD", target = "Lnet/minecraft/item/Item;useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;"), method = "use", cancellable = true)
    private void modify(World world, PlayerEntity playerEntity, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        PlayerUseItemEvent event = new PlayerUseItemEventImpl(playerEntity, world, hand);
        KiloServer.getServer().triggerEvent(event);

        TypedActionResult<ItemStack> actionResult = ItemNbtUtil.onItemUse(world, playerEntity, hand);

        if (actionResult != null)
            cir.setReturnValue(actionResult);

        if (event.isCancelled())
            cir.cancel();
    }

}
