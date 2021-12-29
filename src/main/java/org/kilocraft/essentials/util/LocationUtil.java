package org.kilocraft.essentials.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.world.location.Location;
import org.kilocraft.essentials.api.world.location.Vec3dLocation;
import org.kilocraft.essentials.api.world.location.exceptions.InsecureDestinationException;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.user.UserHomeHandler;
import org.kilocraft.essentials.user.preference.Preferences;
import org.kilocraft.essentials.util.registry.RegistryUtils;

public class LocationUtil {

    public static boolean shouldBlockAccessTo(DimensionType type) {
        return KiloConfig.main().world().disabledDimensions.contains(RegistryUtils.toIdentifier(type).toString()) ||
                KiloConfig.main().world().disabledDimensions.contains(RegistryUtils.toIdentifier(type).getPath());
    }

    public static boolean isBlockSafeFor(OnlineUser user, final Location loc) {
        return user.asPlayer().isCreative() || user.getPreference(Preferences.INVULNERABLE) || (isBlockSafe(loc) && !user.asPlayer().fireImmune());
    }

    public static boolean isBlockSafe(final Location loc) {
        return !canBlockDamage(loc.down()) && isBlockSolid(loc.down());
    }

    public static boolean canBlockDamage(final Location loc) {
        BlockState state = loc.getWorld().getBlockState(loc.toPos());

        if (!KiloEssentials.getMinecraftServer().getGameRules().getBoolean(GameRules.RULE_FIRE_DAMAGE)) {
            return false;
        }

        return state.getMaterial().isFlammable();
    }

    public static boolean isBlockLiquid(final Location loc) {
        return loc.getWorld().getBlockState(loc.toPos()).getMaterial().isLiquid();
    }

    public static boolean isBlockSolid(final Location loc) {
        return loc.getWorld().getBlockState(loc.toPos()).getMaterial().isSolid();
    }

    public static void validateIsSafe(@NotNull final Location loc) throws InsecureDestinationException {
        ServerLevel world = loc.getWorld();
        Vec3dLocation vector = (Vec3dLocation) loc;
        BlockPos pos;
        BlockState state;
        int tries = 0;
        boolean hasAirSpace;
        boolean isNether = RegistryUtils.isNether(loc.getDimensionType());
        boolean safe;

        do {
            tries++;
            vector = (Vec3dLocation) LocationUtil.posOnGround(vector, false);
            pos = vector.toPos();
            state = world.getBlockState(pos);
            Material material = state.getMaterial();
            Biome.BiomeCategory category = world.getBiome(pos).getBiomeCategory();

            if (!LocationUtil.hasSolidGround(vector)) {
                safe = false;
                continue;
            }

            hasAirSpace = !isNether || world.getBlockState(pos.above()).isAir();
            safe = hasAirSpace && !material.isLiquid() && material != Material.FIRE &&
                    category != Biome.BiomeCategory.OCEAN && category != Biome.BiomeCategory.RIVER &&
                    !isBlockLiquid(vector.down());

        } while (tries <= 5 && !safe);

        if (!safe) {
            throw new InsecureDestinationException("The destination is not safe!");
        }
    }

    public static boolean isDestinationToClose(OnlineUser user, Location destination) {
        // We can only check the distance if the locations are in the same dimension
        int minDistance = KiloConfig.main().server().minTeleportDistance;
        if (user.getLocation().getDimension().equals(destination.getDimension()) && minDistance > 0) {
            double distance = Math.sqrt(user.getLocation().squaredDistanceTo(destination));
            if (distance < minDistance) {
                user.sendLangMessage("teleport.too_close", ModConstants.DECIMAL_FORMAT.format(distance), minDistance);
                return true;
            }
        }
        return false;
    }

    public static void processDimension(ServerPlayer player) {
        boolean kickFromDim = KiloConfig.main().world().kickFromDimension;

        if (kickFromDim && LocationUtil.shouldBlockAccessTo(player.getLevel().dimensionType()) && player.getServer() != null) {
            BlockPos pos = player.getRespawnPosition();
            DimensionType dim = RegistryUtils.toDimension(player.getRespawnDimension());

            if (pos == null) {
                OnlineUser user = KiloEssentials.getUserManager().getOnline(player);
                if (user.getLastSavedLocation() != null) {
                    pos = user.getLastSavedLocation().toPos();
                    if (pos == null) {
                        UserHomeHandler homeHandler = user.getHomesHandler();
                        if (homeHandler != null && homeHandler.getHomes().get(0) != null && homeHandler.getHomes().get(0).getLocation().getDimensionType() != player.getLevel().dimensionType()) {
                            pos = user.getHomesHandler().getHomes().get(0).getLocation().toPos();
                        }
                    }
                }
            }

            if (pos != null) {
                KiloEssentials.getUserManager().getOnline(player).sendLangMessage("general.dimension_not_allowed", RegistryUtils.dimensionToName(player.getLevel().dimensionType()));
                player.teleportTo(RegistryUtils.toServerWorld(dim), pos.getX(), pos.getY(), pos.getZ(), player.getYRot(), player.getXRot());
            }
        }
    }

    public static Location posOnGround(@NotNull final Location loc, boolean passLiquid) {
        int yLevel = getLevelOnGround(loc.toPos(), loc.getWorld());
        Location location = copy(loc);
        location.setY(yLevel);
        return location;
    }

    private static int getLevelOnGround(@NotNull final BlockPos pos, @NotNull BlockGetter view) {
        BlockPos blockPos = new BlockPos(pos.getX(), pos.getY(), pos.getZ());

        BlockState state;
        do {
            if (blockPos.getY() <= 0) {
                return 257;
            }

            blockPos = blockPos.below();
            state = view.getBlockState(blockPos);
        } while (state.isAir());

        return blockPos.getY() + 1;
    }

    public static boolean hasSolidGround(@NotNull final Location loc) {
        BlockPos blockPos = loc.toPos();
        BlockGetter view = loc.getWorld();
        BlockState state;
        Material material;

        do {
            if (blockPos.getY() <= 0) {
                return false;
            }

            blockPos = blockPos.below();
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
        BlockGetter view = loc.getWorld();

        do {
            if (pos.getY() <= 0) {
                return;
            }

            pos = pos.below();
            state = view.getBlockState(pos);
            state2 = view.getBlockState(pos.above());
            state3 = view.getBlockState(pos.above(2));
        } while (!state.isAir() && state2.isAir() && state3.isAir() && (!passLiquid || state.getMaterial().isLiquid()));

        loc.setY(pos.getY());
    }

    public static Location copy(@NotNull final Location loc) {
        return Vec3dLocation.of(loc.getX(), loc.getY(), loc.getZ(), loc.getRotation().getYaw(), loc.getRotation().getPitch(), loc.getDimension());
    }

}
