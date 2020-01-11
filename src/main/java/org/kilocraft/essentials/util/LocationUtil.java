package org.kilocraft.essentials.util;

import net.minecraft.block.BlockState;

public class LocationUtil {

    public static boolean isBlockSafe(final Location loc) {


        return false;
    }

    public static boolean canBlockDamage(final Location loc) {
        BlockState state = loc.getWorld().getBlockState(loc.getPos());

        return false;
    }

}
