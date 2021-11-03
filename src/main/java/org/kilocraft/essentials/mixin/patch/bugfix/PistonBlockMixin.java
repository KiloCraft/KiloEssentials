package org.kilocraft.essentials.mixin.patch.bugfix;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.kilocraft.essentials.util.settings.ServerSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Map;

@Mixin(PistonBlock.class)
public abstract class PistonBlockMixin {

    /**
     * Fix piston physics inconsistency
     */

    @Inject(
            method = "move",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;size()I",
                    ordinal = 3
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void removeOldBlocks(World world, BlockPos pos, Direction dir, boolean retract, CallbackInfoReturnable<Boolean> cir, BlockPos blockPos, PistonHandler pistonHandler, Map<BlockPos, BlockState> map, List<BlockPos> list, List<BlockState> list2, List<BlockPos> list3, BlockState blockStates[], Direction direction, int j) {
        if (ServerSettings.patch_tnt_duping) {
            for (int l = list.size() - 1; l >= 0; --l) {
                BlockPos toBeMovedBlockPos = list.get(l);
                BlockState toBeMovedBlockState = world.getBlockState(toBeMovedBlockPos);
                world.setBlockState(toBeMovedBlockPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS | Block.NO_REDRAW | Block.FORCE_STATE | Block.MOVED);
                list2.set(l, toBeMovedBlockState);
                map.put(toBeMovedBlockPos, toBeMovedBlockState);
            }
        }
    }

    @Inject(
            method = "move",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;keySet()Ljava/util/Set;",
                    ordinal = 0
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void adjustBlockStatesArray(World world, BlockPos pos, Direction dir, boolean retract, CallbackInfoReturnable<Boolean> cir, BlockPos blockPos, PistonHandler pistonHandler, Map<BlockPos, BlockState> map, List<BlockPos> movedBlocks, List<BlockState> oldBlockStates, List<BlockPos> brokenBlocks, BlockState[] blockStates, Direction direction, int i, BlockState blockState6) {
        if (ServerSettings.patch_tnt_duping) {
            int size = brokenBlocks.size();
            for (int j = movedBlocks.size() - 1; j >= 0; --j) {
                blockStates[size++] = oldBlockStates.get(j);
            }
        }
    }

}
