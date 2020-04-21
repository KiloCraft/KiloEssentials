package org.kilocraft.essentials.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.world.location.Location;
import org.kilocraft.essentials.api.world.location.Vec3dLocation;
import org.kilocraft.essentials.api.world.location.exceptions.InsecureDestinationException;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.user.setting.Settings;
import org.kilocraft.essentials.util.text.Texter;

public class LocationUtil {
    public static int MAX_WORLD_HEIGHT = KiloServer.getServer().getVanillaServer().getWorldHeight();

    public static boolean isDimensionValid(DimensionType type) {
        return type == DimensionType.OVERWORLD ||
                ((type == DimensionType.THE_NETHER && KiloConfig.main().world().allowTheNether) ||
                        type == DimensionType.THE_END && KiloConfig.main().world().allowTheEnd);
    }

    public static boolean isBlockSafeFor(OnlineUser user, final Location loc) {
        return user.asPlayer().isCreative() || user.getSetting(Settings.INVULNERABLE) || (isBlockSafe(loc) && !user.asPlayer().isFireImmune());
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

    public static void validateIsSafe(@NotNull final Location loc) throws InsecureDestinationException {
        ServerWorld world = loc.getWorld();
        Vec3dLocation vector = (Vec3dLocation) loc;
        BlockPos pos;
        BlockState state;
        int tries = 0;
        boolean hasAirSpace;
        boolean isNether = world.dimension.isNether();
        boolean safe;

        do {
            tries++;
            vector = (Vec3dLocation) LocationUtil.posOnGround(vector, false);
            pos = vector.toPos();
            state = world.getBlockState(pos);
            Material material = state.getMaterial();
            Biome.Category category = world.getBiome(pos).getCategory();

            if (!LocationUtil.hasSolidGround(vector)) {
                safe = false;
                continue;
            }

            hasAirSpace = !isNether || world.getBlockState(pos.up()).isAir();
            safe = hasAirSpace && !material.isLiquid() && material != Material.FIRE &&
                    category != Biome.Category.OCEAN && category != Biome.Category.RIVER &&
                    !isBlockLiquid(vector.down());

        } while (tries <= 5 && !safe);

        if (!safe) {
            throw new InsecureDestinationException("The destination is not safe!");
        }
    }

    public static Location posOnGround(@NotNull final Location loc, boolean passLiquid) {
        int yLevel = getLevelOnGround(loc.toPos(), loc.getWorld());
        Location location = copy(loc);
        location.setY(yLevel);
        return location;
    }

    private static int getLevelOnGround(@NotNull final BlockPos pos, @NotNull BlockView view) {
        BlockPos blockPos = new BlockPos(pos.getX(), pos.getY(), pos.getZ());

        BlockState state;
        do {
            if (blockPos.getY() <= 0) {
                return 257;
            }

            blockPos = blockPos.down();
            state = view.getBlockState(blockPos);
        } while (state.isAir());

        return blockPos.getY() + 1;
    }

    public static boolean hasSolidGround(@NotNull final Location loc) {
        BlockPos blockPos = loc.toPos();
        BlockView view = loc.getWorld();
        BlockState state;
        Material material;

        do {
            if (blockPos.getY() <= 0) {
                return false;
            }

            blockPos = blockPos.down();
            state = view.getBlockState(blockPos);
            material = state.getMaterial();

            if (material.isSolid()) {
                return true;
            }

        } while (state.isAir() && !material.isSolid());

        return false;
    }

    public static void posOnGroundWothAirSpaceOnTop(@NotNull final Location loc, boolean passLiquid) {
        BlockPos pos = loc.toPos();
        BlockState state, state2, state3;
        BlockView view = loc.getWorld();

        do {
            if (pos.getY() <= 0) {
                return;
            }

            pos = pos.down();
            state = view.getBlockState(pos);
            state2 = view.getBlockState(pos.up());
            state3 = view.getBlockState(pos.up(2));
        } while (!state.isAir() && state2.isAir() && state3.isAir() && (!passLiquid || state.getMaterial().isLiquid()));

        loc.setY(pos.getY());
    }

    public static Location copy(@NotNull final Location loc) {
        return Vec3dLocation.of(loc.getX(), loc.getY(), loc.getZ(), loc.getRotation().getYaw(), loc.getRotation().getPitch(), loc.getDimension());
    }

}
