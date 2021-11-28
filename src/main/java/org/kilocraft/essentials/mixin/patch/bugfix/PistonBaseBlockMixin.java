package org.kilocraft.essentials.mixin.patch.bugfix;

import org.kilocraft.essentials.util.settings.ServerSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(PistonBaseBlock.class)
public abstract class PistonBaseBlockMixin {

    /**
     * Fix piston physics inconsistency
     */

    @Inject(
            method = "moveBlocks",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;size()I",
                    ordinal = 3
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void removeOldBlocks(Level world, BlockPos pos, Direction dir, boolean retract, CallbackInfoReturnable<Boolean> cir, BlockPos blockPos, PistonStructureResolver pistonHandler, Map<BlockPos, BlockState> map, List<BlockPos> list, List<BlockState> list2, List<BlockPos> list3, BlockState blockStates[], Direction direction, int j) {
        if (ServerSettings.patch_tnt_duping) {
            for (int l = list.size() - 1; l >= 0; --l) {
                BlockPos toBeMovedBlockPos = list.get(l);
                BlockState toBeMovedBlockState = world.getBlockState(toBeMovedBlockPos);
                world.setBlock(toBeMovedBlockPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_INVISIBLE | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_MOVE_BY_PISTON);
                list2.set(l, toBeMovedBlockState);
                map.put(toBeMovedBlockPos, toBeMovedBlockState);
            }
        }
    }

    @Inject(
            method = "moveBlocks",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;keySet()Ljava/util/Set;",
                    ordinal = 0
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void adjustBlockStatesArray(Level world, BlockPos pos, Direction dir, boolean retract, CallbackInfoReturnable<Boolean> cir, BlockPos blockPos, PistonStructureResolver pistonHandler, Map<BlockPos, BlockState> map, List<BlockPos> movedBlocks, List<BlockState> oldBlockStates, List<BlockPos> brokenBlocks, BlockState[] blockStates, Direction direction, int i, BlockState blockState6) {
        if (ServerSettings.patch_tnt_duping) {
            int size = brokenBlocks.size();
            for (int j = movedBlocks.size() - 1; j >= 0; --j) {
                blockStates[size++] = oldBlockStates.get(j);
            }
        }
    }

}
