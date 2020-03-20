package org.kilocraft.essentials.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.world.location.Location;
import org.kilocraft.essentials.api.world.location.Vec3dLocation;
import org.kilocraft.essentials.config.KiloConfig;

public class LocationUtil {
    public static int MAX_WORLD_HEIGHT = KiloServer.getServer().getVanillaServer().getWorldHeight();

    public static boolean isDimensionValid(DimensionType type) {
        return type == DimensionType.OVERWORLD ||
                ((type == DimensionType.THE_NETHER && KiloConfig.main().world().allowTheNether) ||
                        type == DimensionType.THE_END && KiloConfig.main().world().allowTheEnd);
    }

    public static boolean isBlockSafeFor(OnlineUser user, final Location loc) {
        return user.getPlayer().isCreative() || user.isInvulnerable() || (isBlockSafe(loc) && !user.getPlayer().isFireImmune());
    }

    public static boolean isBlockSafe(final Location loc) {
        return !canBlockDamage(loc);
    }

    public static boolean canBlockDamage(final Location loc) {
        BlockState state = loc.getWorld().getBlockState(loc.toPos());

        if (!KiloServer.getServer().getVanillaServer().getGameRules().getBoolean(GameRules.FIRE_DAMAGE)) {
            return false;
        }

        return state.getMaterial().isBurnable();
    }

    public static boolean isBlockLiquid(final Location loc) {
        BlockState state = loc.getWorld().getBlockState(loc.toPos());

        if (!KiloServer.getServer().getVanillaServer().getGameRules().getBoolean(GameRules.FIRE_DAMAGE)) {
            return false;
        }

        return state.getMaterial().isLiquid();
    }

    public static Location getPosOnGround(@NotNull final Location loc) {
        BlockPos pos = new BlockPos(loc.getX(), 256.0D , loc.getZ());
        BlockState state;

        do {
            pos = pos.down();
            state = loc.getWorld().getBlockState(pos);
        } while (state.isAir());

        return Vec3dLocation.of(pos.getX(), pos.getZ(), pos.getZ());
    }

}
