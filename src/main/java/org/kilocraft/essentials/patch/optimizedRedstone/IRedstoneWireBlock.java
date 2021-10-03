package org.kilocraft.essentials.patch.optimizedRedstone;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IRedstoneWireBlock {

    BlockState calculateCurrentChanges(World worldIn, BlockPos pos1, BlockPos pos2, BlockState state);

}
