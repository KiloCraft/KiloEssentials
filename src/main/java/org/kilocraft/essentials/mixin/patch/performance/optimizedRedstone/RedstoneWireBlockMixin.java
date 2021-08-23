package org.kilocraft.essentials.mixin.patch.performance.optimizedRedstone;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.kilocraft.essentials.patch.optimizedRedstone.RedstoneWireBlockInterface;
import org.kilocraft.essentials.patch.optimizedRedstone.RedstoneWireTurbo;
import org.kilocraft.essentials.util.settings.ServerSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RedstoneWireBlock.class)
public abstract class RedstoneWireBlockMixin implements RedstoneWireBlockInterface {

    @Shadow
    protected abstract int increasePower(BlockState state);

    @Shadow
    private boolean wiresGivePower;

    @Shadow protected abstract void update(World world, BlockPos pos, BlockState state);

    RedstoneWireTurbo turbo = new RedstoneWireTurbo((RedstoneWireBlock) (Object) this);

    @Override
    public BlockState calculateCurrentChanges(World worldIn, BlockPos pos1, BlockPos pos2, BlockState state) {
        BlockState iblockstate = state;
        int i = state.get(RedstoneWireBlock.POWER);
        int j = 0;
        j = this.getPower(j, worldIn.getBlockState(pos2));
        this.wiresGivePower = false;
        int k = worldIn.getReceivedRedstonePower(pos1);
        this.wiresGivePower = true;

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
            for (Direction enumfacing : Direction.Type.HORIZONTAL) {
                BlockPos blockpos = pos1.offset(enumfacing);
                boolean flag = blockpos.getX() != pos2.getX() || blockpos.getZ() != pos2.getZ();

                if (flag) {
                    l = this.getPower(l, worldIn.getBlockState(blockpos));
                }

                if (worldIn.getBlockState(blockpos).isSolidBlock(worldIn, blockpos) && !worldIn.getBlockState(pos1.up()).isSolidBlock(worldIn, pos1)) {
                    if (flag && pos1.getY() >= pos2.getY()) {
                        l = this.getPower(l, worldIn.getBlockState(blockpos.up()));
                    }
                } else if (!worldIn.getBlockState(blockpos).isSolidBlock(worldIn, blockpos) && flag && pos1.getY() <= pos2.getY()) {
                    l = this.getPower(l, worldIn.getBlockState(blockpos.down()));
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
            state = state.with(RedstoneWireBlock.POWER, j);

            if (worldIn.getBlockState(pos1) == iblockstate) {
                worldIn.setBlockState(pos1, state, 2);
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
    private void updateSurroundingRedstone(World worldIn, BlockPos pos, BlockState state, BlockPos source) {
        if (ServerSettings.patch_eigencraft_redstone) {
            turbo.updateSurroundingRedstone(worldIn, pos, state, source);
            return;
        }
        update(worldIn, pos, state);
    }

    private int getPower(int min, BlockState iblockdata) {
        return Math.max(min, increasePower(iblockdata));
    }

    @Redirect(method = "onBlockAdded", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/RedstoneWireBlock;update(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V"))
    public void optimizeRedstoneUpdate(RedstoneWireBlock redstoneWireBlock, World world, BlockPos pos, BlockState state) {
        updateSurroundingRedstone(world, pos, state, null);
    }

    @Redirect(method = "onStateReplaced", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/RedstoneWireBlock;update(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V"))
    public void optimizeRedstoneUpdate$2(RedstoneWireBlock redstoneWireBlock, World world, BlockPos pos, BlockState state) {
        updateSurroundingRedstone(world, pos, state, null);
    }

    @Redirect(method = "neighborUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/RedstoneWireBlock;update(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V"))
    public void optimizeRedstoneUpdate$3(RedstoneWireBlock redstoneWireBlock, World world, BlockPos pos, BlockState state, BlockState state2, World world2, BlockPos pos2, Block block, BlockPos fromPos, boolean notify) {
        updateSurroundingRedstone(world, pos, state, fromPos);
    }
}
