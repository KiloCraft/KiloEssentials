package org.kilocraft.essentials.util;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.user.OnlineUser;

public class LocationUtil {

    public static boolean isBlockSafeFor(OnlineUser user, final LocationImpl loc) {
        return user.getPlayer().isCreative() || user.isInvulnerable() || isBlockSafe(loc);
    }

    public static boolean isBlockSafe(final LocationImpl loc) {
        return canBlockDamage(loc);
    }

    public static boolean canBlockDamage(final LocationImpl loc) {
        Block block = loc.getWorld().getBlockState(loc.getPos()).getBlock();

        if (!KiloServer.getServer().getVanillaServer().getGameRules().getBoolean(GameRules.FIRE_DAMAGE))
            return false;

        return block == Blocks.LAVA || block == Blocks.FIRE || block == Blocks.MAGMA_BLOCK;
    }

    @Nullable
    public static BlockPos getPosOnGround(final LocationImpl loc) {
        if (loc.getDimension() == null)
            return null;

        BlockPos finalPos = loc.getPos();
        int blocksLeft = LocationImpl.MAX_BUILD_LIMIT - loc.getY();

        for (int i = 0; i < blocksLeft; i++) {
            BlockPos pos = new BlockPos(loc.getX(), i, loc.getZ());

            if (loc.getWorld().getBlockState(pos).isAir())
                continue;

            if (isBlockSafe(loc))
                finalPos = pos;
        }

        return finalPos;
    }

}
