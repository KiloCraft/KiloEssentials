package org.kilocraft.essentials.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
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
        return !canBlockDamage(loc.down()) && isBlockSolid(loc.down());
    }

    public static boolean canBlockDamage(final Location loc) {
        BlockState state = loc.getWorld().getBlockState(loc.toPos());

        if (!KiloServer.getServer().getVanillaServer().getGameRules().getBoolean(GameRules.FIRE_DAMAGE)) {
            return false;
        }

        return state.getMaterial().isBurnable();
    }

    public static boolean isBlockLiquid(final Location loc) {
        return loc.getWorld().getBlockState(loc.toPos()).getMaterial().isLiquid();
    }

    public static boolean isBlockSolid(final Location loc) {
        return loc.getWorld().getBlockState(loc.toPos()).getMaterial().isSolid();
    }

    @Nullable
    public static Location posOnGround(@NotNull final Location loc, boolean passLiquid) {
        BlockPos pos = loc.toPos();
        BlockState state;
        BlockView view = loc.getWorld();

        do {
            if (pos.getY() <= 0) {
                return null;
            }

            pos = pos.down();
            state = view.getBlockState(pos);
        } while (state.isAir() && (!passLiquid || state.getMaterial().isLiquid()));

        loc.setY(pos.getY());
        return loc;
    }

    @Nullable
    public static Location posOnGroundWothAirSpaceOnTop(@NotNull final Location loc, boolean passLiquid) {
        BlockPos pos = loc.toPos();
        BlockState state, state2, state3;
        BlockView view = loc.getWorld();

        do {
            if (pos.getY() <= 0) {
                return null;
            }

            pos = pos.down();
            state = view.getBlockState(pos);
            state2 = view.getBlockState(pos.up());
            state3 = view.getBlockState(pos.up(2));
        } while (!state.isAir() && state2.isAir() && state3.isAir() && (!passLiquid || state.getMaterial().isLiquid()));

        loc.setY(pos.getY());
        return loc;
    }

}
