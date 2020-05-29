package org.kilocraft.essentials.util.registry;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.util.StringUtils;

import java.util.Objects;
import java.util.Set;

public class RegistryUtils {
    private static final MinecraftServer server = KiloServer.getServer().getMinecraftServer();
    private static final RegistryKey<World> DEFAULT_WORLD_KEY = Worlds.OVERWORLD;

    public static ServerWorld toServerWorld(@NotNull final DimensionType type) {
        return server.getWorld(toWorldKey(type));
    }

    public static Identifier toIdentifier(@NotNull final DimensionType type) {
        RegistryKey<World> key = dimensionTypeToRegistryKey(type);
        return key == null ? Objects.requireNonNull(dimensionTypeToRegistryKey(toDimension(DEFAULT_WORLD_KEY))).getValue() : key.getValue();
    }

    public static RegistryKey<World> toWorldKey(@NotNull final DimensionType type) {
        return toServerWorld(type).method_27983();
    }

    public static DimensionType toDimension(@NotNull final Identifier identifier) {
        for (RegistryKey<World> worldRegistryKey : getWorldsKeySet()) {
            if (worldRegistryKey.getValue().equals(identifier)) {
                return toDimension(worldRegistryKey);
            }
        }

        return null;
    }

    public static DimensionType toDimension(@NotNull final RegistryKey<World> key) {
        return server.getWorld(key).getDimension();
    }

    public static String dimensionToName(@NotNull final Identifier identifier) {
        return dimensionToName(toDimension(identifier));
    }

    public static String dimensionToName(@NotNull final DimensionType type) {
        RegistryKey<World> key = dimensionTypeToRegistryKey(type);
        if (key == null) {
            return String.valueOf((Object) null);
        }

        return key == World.field_25179 ? "Overworld"
                : key == World.field_25180 ? "The Nether"
                : key == World.field_25181 ? "The End"
                : StringUtils.normalizeCapitalization(key.getValue().getPath());
    }

    @Nullable
    public static RegistryKey<World> dimensionTypeToRegistryKey(@NotNull final DimensionType type) {
        for (RegistryKey<World> worldRegistryKey : getWorldsKeySet()) {
            final DimensionType dim = server.getWorld(worldRegistryKey).getDimension();
            if (dim.equals(type)) {
                return worldRegistryKey;
            }
        }

        return null;
    }

    public static Set<RegistryKey<World>> getWorldsKeySet() {
        return server.method_29435();
    }

    public static class Worlds {
        public static final RegistryKey<World> OVERWORLD = World.field_25179;
        public static final RegistryKey<World> THE_NETHER = World.field_25180;
        public static final RegistryKey<World> THE_END = World.field_25181;
    }
}
