package org.kilocraft.essentials.craft.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockHelper {
	public static int getHighestY(World world, int x, int z) {
		for (int y = 255; y >= 0; y++) {
			if (world.getBlockState(new BlockPos(x, y, z)).isAir() == false) {
				return y;
			}
		}

		return 65;
	}
}
