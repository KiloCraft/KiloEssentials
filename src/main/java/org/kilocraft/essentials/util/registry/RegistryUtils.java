package org.kilocraft.essentials.util.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.util.StringUtils;

import java.util.Objects;
import java.util.Set;

public class RegistryUtils {
    private static final MinecraftServer server = KiloEssentials.getMinecraftServer();
    private static final ResourceKey<Level> DEFAULT_WORLD_KEY = Level.OVERWORLD;

    @Nullable
    public static ServerLevel toServerWorld(@NotNull final DimensionType type) {
        for (ServerLevel world : server.getAllLevels()) {
            if (world.dimensionType() == type) {
                return world;
            }
        }

        return null;
    }

    public static ResourceLocation toIdentifier(@NotNull final DimensionType type) {
        ResourceKey<Level> key = dimensionTypeToRegistryKey(type);
        return key == null ? Objects.requireNonNull(dimensionTypeToRegistryKey(toDimension(DEFAULT_WORLD_KEY))).location() : key.location();
    }

    @Nullable
    public static ResourceKey<Level> toWorldKey(@NotNull final DimensionType type) {
        return toServerWorld(type) == null ? null : Objects.requireNonNull(toServerWorld(type)).dimension();
    }

    public static DimensionType toDimension(@NotNull final ResourceLocation identifier) {
        for (ResourceKey<Level> worldRegistryKey : getWorldsKeySet()) {
            if (worldRegistryKey.location().equals(identifier)) {
                return toDimension(worldRegistryKey);
            }
        }

        return null;
    }

    public static DimensionType toDimension(@NotNull final ResourceKey<Level> key) {
        return server.getLevel(key).dimensionType();
    }

    public static String dimensionToName(@NotNull final ResourceLocation identifier) {
        return dimensionToName(toDimension(identifier));
    }

    public static String dimensionToName(@Nullable final DimensionType type) {
        ResourceKey<Level> key = type == null ? null : dimensionTypeToRegistryKey(type);
        if (key == null) {
            return String.valueOf((Object) null);
        }

        return key == Level.OVERWORLD ? "Overworld"
                : key == Level.NETHER ? "The Nether"
                : key == Level.END ? "The End"
                : StringUtils.normalizeCapitalization(key.location().getPath());
    }

    @Nullable
    public static ResourceKey<Level> dimensionTypeToRegistryKey(@NotNull final DimensionType type) {
        for (ResourceKey<Level> worldRegistryKey : getWorldsKeySet()) {
            final DimensionType dim = server.getLevel(worldRegistryKey).dimensionType();
            if (dim.equalTo(type)) {
                return worldRegistryKey;
            }
        }

        return null;
    }

    public static Set<ResourceKey<Level>> getWorldsKeySet() {
        return server.levelKeys();
    }

    public static boolean isOverworld(@NotNull final DimensionType type) {
        return type == toDimension(DimensionType.OVERWORLD_LOCATION.location());
    }

    public static boolean isNether(@NotNull final DimensionType type) {
        return type == toDimension(DimensionType.NETHER_LOCATION.location());
    }

    public static boolean isEnd(@NotNull final DimensionType type) {
        return type == toDimension(DimensionType.END_LOCATION.location());
    }

    public static String toIdentifier(@NotNull Item item) {
        return Registry.ITEM.getKey(item).toString();
    }

    @Nullable
    public static Item toItem(@NotNull String item) {
        return Registry.ITEM.get(new ResourceLocation(item));
    }

}
