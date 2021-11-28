package org.kilocraft.essentials.patch.optimizedRedstone;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IRedstoneWireBlock {

    BlockState calculateCurrentChanges(Level worldIn, BlockPos pos1, BlockPos pos2, BlockState state);

}
