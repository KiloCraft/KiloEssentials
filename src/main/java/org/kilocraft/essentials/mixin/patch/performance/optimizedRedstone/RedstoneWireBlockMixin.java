package org.kilocraft.essentials.mixin.patch.performance.optimizedRedstone;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.kilocraft.essentials.patch.optimizedRedstone.IRedstoneWireBlock;
import org.kilocraft.essentials.patch.optimizedRedstone.RedstoneWireTurbo;
import org.kilocraft.essentials.util.settings.ServerSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RedStoneWireBlock.class)
public abstract class RedstoneWireBlockMixin implements IRedstoneWireBlock {

    @Shadow
    private boolean shouldSignal;

    @Shadow
    protected abstract void updatePowerStrength(Level level, BlockPos blockPos, BlockState blockState);

    @Shadow
    protected abstract int getWireSignal(BlockState blockState);

    RedstoneWireTurbo turbo = new RedstoneWireTurbo((RedStoneWireBlock) (Object) this);

    @Override
    public BlockState calculateCurrentChanges(Level worldIn, BlockPos pos1, BlockPos pos2, BlockState state) {
        BlockState iblockstate = state;
        int i = state.getValue(RedStoneWireBlock.POWER);
        int j = 0;
        j = this.getPower(j, worldIn.getBlockState(pos2));
        this.shouldSignal = false;
        int k = worldIn.getBestNeighborSignal(pos1);
        this.shouldSignal = true;

        if (!ServerSettings.patch_eigencraft_redstone) {
            // This code is totally redundant to if statements just below the loop.
            if (k > 0 && k > j - 1) {
                j = k;
            }
        }

        int l = 0;

        // The variable 'k' holds the maximum redstone power value of any adjacent blocks.
        // If 'k' has the highest level of all neighbors, then the power level of this
        // redstone wire will be set to 'k'.  If 'k' is already 15, then nothing inside the
        // following loop can affect the power level of the wire.  Therefore, the loop is
        // skipped if k is already 15.
        if (!ServerSettings.patch_eigencraft_redstone || k < 15) {
            for (Direction enumfacing : Direction.Plane.HORIZONTAL) {
                BlockPos blockpos = pos1.relative(enumfacing);
                boolean flag = blockpos.getX() != pos2.getX() || blockpos.getZ() != pos2.getZ();

                if (flag) {
                    l = this.getPower(l, worldIn.getBlockState(blockpos));
                }

                if (worldIn.getBlockState(blockpos).isRedstoneConductor(worldIn, blockpos) && !worldIn.getBlockState(pos1.above()).isRedstoneConductor(worldIn, pos1)) {
                    if (flag && pos1.getY() >= pos2.getY()) {
                        l = this.getPower(l, worldIn.getBlockState(blockpos.above()));
                    }
                } else if (!worldIn.getBlockState(blockpos).isRedstoneConductor(worldIn, blockpos) && flag && pos1.getY() <= pos2.getY()) {
                    l = this.getPower(l, worldIn.getBlockState(blockpos.below()));
                }
            }
        }

        if (!ServerSettings.patch_eigencraft_redstone) {
            // The old code would decrement the wire value only by 1 at a time.
            if (l > j) {
                j = l - 1;
            } else if (j > 0) {
                --j;
            } else {
                j = 0;
            }

            if (k > j - 1) {
                j = k;
            }
        } else {
            // The new code sets this RedstoneWire block's power level to the highest neighbor
            // minus 1.  This usually results in wire power levels dropping by 2 at a time.
            // This optimization alone has no impact on update order, only the number of updates.
            j = l - 1;

            // If 'l' turns out to be zero, then j will be set to -1, but then since 'k' will
            // always be in the range of 0 to 15, the following if will correct that.
            if (k > j) j = k;
        }

        if (i != j) {
            state = state.setValue(RedStoneWireBlock.POWER, j);

            if (worldIn.getBlockState(pos1) == iblockstate) {
                worldIn.setBlock(pos1, state, 2);
            }

            // 1.16(.1?) dropped the need for blocks needing updates.
            // Whether this is necessary after all is to be seen.
//            if (!worldIn.paperConfig.useEigencraftRedstone) {
//                // The new search algorithm keeps track of blocks needing updates in its own data structures,
//                // so only add anything to blocksNeedingUpdate if we're using the vanilla update algorithm.
//                this.getBlocksNeedingUpdate().add(pos1);
//
//                for (EnumDirection enumfacing1 : EnumDirection.values()) {
//                    this.getBlocksNeedingUpdate().add(pos1.shift(enumfacing1));
//                }
//            }
        }

        return state;
    }

    /*
     * Modified version of pre-existing updateSurroundingRedstone, which is called from
     * this.neighborChanged and a few other methods in this class.
     * Note: Added 'source' argument so as to help determine direction of information flow
     */
    private void updateSurroundingRedstone(Level worldIn, BlockPos pos, BlockState state, BlockPos source) {
        if (ServerSettings.patch_eigencraft_redstone) {
            this.turbo.updateSurroundingRedstone(worldIn, pos, state, source);
            return;
        }
        this.updatePowerStrength(worldIn, pos, state);
    }

    private int getPower(int min, BlockState iblockdata) {
        return Math.max(min, this.getWireSignal(iblockdata));
    }

    @Redirect(
            method = "onPlace",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/RedStoneWireBlock;updatePowerStrength(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V"
            )
    )
    public void optimizeRedstoneUpdate(RedStoneWireBlock redstoneWireBlock, Level world, BlockPos pos, BlockState state) {
        this.updateSurroundingRedstone(world, pos, state, null);
    }

    @Redirect(
            method = "onRemove",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/RedStoneWireBlock;updatePowerStrength(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V"
            )
    )
    public void optimizeRedstoneUpdate$2(RedStoneWireBlock redstoneWireBlock, Level world, BlockPos pos, BlockState state) {
        this.updateSurroundingRedstone(world, pos, state, null);
    }

    @Redirect(
            method = "neighborChanged",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/RedStoneWireBlock;updatePowerStrength(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V"
            )
    )
    public void optimizeRedstoneUpdate$3(RedStoneWireBlock redstoneWireBlock, Level world, BlockPos pos, BlockState state, BlockState state2, Level world2, BlockPos pos2, Block block, BlockPos fromPos, boolean notify) {
        this.updateSurroundingRedstone(world, pos, state, fromPos);
    }
}
