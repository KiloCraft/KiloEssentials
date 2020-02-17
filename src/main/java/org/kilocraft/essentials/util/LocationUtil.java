package org.kilocraft.essentials.util;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.world.location.Location;
import org.kilocraft.essentials.config.KiloConfig;

public class LocationUtil {
    public static int MAX_WORLD_HEIGHT = KiloServer.getServer().getVanillaServer().getWorldHeight();

    public static boolean isDimensionValid(DimensionType type) {
        return type == DimensionType.OVERWORLD ||
                ((type == DimensionType.THE_NETHER && KiloConfig.main().world().allowTheNether) ||
                        type == DimensionType.THE_END && KiloConfig.main().world().allowTheEnd);
    }

    public static boolean isBlockSafeFor(OnlineUser user, final Location loc) {
        return user.getPlayer().isCreative() || user.isInvulnerable() || isBlockSafe(loc);
    }

    public static boolean isBlockSafe(final Location loc) {
        return canBlockDamage(loc);
    }

    public static boolean canBlockDamage(final Location loc) {
        Block block = loc.getWorld().getBlockState(loc.toPos()).getBlock();

        if (!KiloServer.getServer().getVanillaServer().getGameRules().getBoolean(GameRules.FIRE_DAMAGE))
            return false;

        return block == Blocks.LAVA || block == Blocks.FIRE || block == Blocks.MAGMA_BLOCK;
    }

    @Nullable
    public static BlockPos getPosOnGround(final Location loc) {
        if (loc.getDimension() == null)
            return null;

        BlockPos finalPos = loc.toPos();
        int blocksLeft = MAX_WORLD_HEIGHT - (int) loc.getY();

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
