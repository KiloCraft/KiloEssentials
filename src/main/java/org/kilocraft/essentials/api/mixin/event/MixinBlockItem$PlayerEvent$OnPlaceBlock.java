package org.kilocraft.essentials.api.mixin.event;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.event.eventImpl.playerEventsImpl.PlayerEvent$OnPlaceBlockImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class MixinBlockItem$PlayerEvent$OnPlaceBlock {

    @Shadow
    public abstract Block getBlock();

    @Shadow
    @Nullable
    protected abstract BlockState getPlacementState(ItemPlacementContext itemPlacementContext_1);

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemPlacementContext;getBlockPos()Lnet/minecraft/util/math/BlockPos;"), method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", cancellable = true)
    private void oky$place(ItemPlacementContext itemPlacementContext_1, CallbackInfoReturnable<ActionResult> cir) {
        ServerPlayerEntity player = (ServerPlayerEntity) itemPlacementContext_1.getPlayer();

        BlockPos mcPos = itemPlacementContext_1.getBlockPos();
        Vec3d pos = new Vec3d(mcPos.getX(), mcPos.getY(), mcPos.getZ());

//        org.kilocraft.essentials.api.world.worldimpl.BlockImpl block = new BlockImpl(new WorldImpl(itemPlacementContext_1.getWorld()), getPlacementState(itemPlacementContext_1), pos);
        PlayerEvent$OnPlaceBlockImpl e = KiloServer.getServer().triggerEvent(
                new PlayerEvent$OnPlaceBlockImpl(itemPlacementContext_1, player, getBlock()
                ));

        if (e.isCancelled()) {
            player.world.setBlockState(itemPlacementContext_1.getBlockPos(), Blocks.AIR.getDefaultState()); // This might be bad, not sure

            cir.setReturnValue(ActionResult.FAIL);
            cir.cancel();
        }
    }
}
