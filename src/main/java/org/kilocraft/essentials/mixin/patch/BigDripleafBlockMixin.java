package org.kilocraft.essentials.mixin.patch;

import net.minecraft.block.*;
import net.minecraft.fluid.Fluids;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.kilocraft.essentials.util.settings.ServerSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Random;

@Mixin(BigDripleafBlock.class)
public class BigDripleafBlockMixin {

    @Shadow
    @Final
    private static BooleanProperty WATERLOGGED;

    /**
     * @author Drex
     * @reason Fixes https://bugs.mojang.com/browse/MC-213813
     */
    @Overwrite
    protected static void grow(World world, Random random, BlockPos blockPos) {
        int i = world.getTopHeightLimit() - blockPos.getY();
        int j = 1 + random.nextInt(ServerSettings.DRIP_LEAF_HEIGHT.getValue());
        int k = Math.min(j, i);
        Direction direction = Direction.Type.HORIZONTAL.random(random);
        BlockPos.Mutable mutable = blockPos.mutableCopy();
        boolean end = false;
        int l = 0;
        while (l < k && !end) {
            BlockPos.Mutable above = mutable.mutableCopy().move(Direction.UP);
            if (l == k - 1 || (!world.getBlockState(above).isAir() && !world.getBlockState(above).isOf(Blocks.SMALL_DRIPLEAF))) {
                end = true;
            }
            Block block = end ? Blocks.BIG_DRIPLEAF : Blocks.BIG_DRIPLEAF_STEM;
            BlockState blockState = block.getDefaultState().with(WATERLOGGED, world.getFluidState(mutable).getFluid() == Fluids.WATER).with(HorizontalFacingBlock.FACING, direction);
            world.setBlockState(mutable, blockState, 2);
            mutable.move(Direction.UP);
            l++;
        }
    }
}
