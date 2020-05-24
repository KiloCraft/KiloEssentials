package org.kilocraft.essentials.util.registry;

import com.google.common.collect.Lists;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.util.StringUtils;

import java.util.List;
import java.util.Objects;

public class RegistryUtils {
    private static final List<RegistryKey<DimensionType>> registryKeys = Lists.newArrayList();

    public static ServerWorld toServerWorld(DimensionType type) {
        return KiloServer.getServer().getWorld(dimensionTypeToRegistryKey(type));
    }

    public static Identifier toIdentifier(DimensionType type) {
        RegistryKey<DimensionType> key = dimensionTypeToRegistryKey(type);
        return key == null ? Objects.requireNonNull(dimensionTypeToRegistryKey(DimensionType.getDefaultDimensionType())).getValue() : key.getValue();
    }

    public static DimensionType toDimension(Identifier identifier) {
        return KiloServer.getServer().getMinecraftServer().method_29174().getRegistry().get(identifier);
    }

    public static DimensionType toDimension(RegistryKey<DimensionType> key) {
        return KiloServer.getServer().getMinecraftServer().method_29174().getRegistry().get(key);
    }

    public static String dimensionToName(Identifier identifier) {
        return dimensionToName(toDimension(identifier));
    }

    public static String dimensionToName(DimensionType type) {
        RegistryKey<DimensionType> key = dimensionTypeToRegistryKey(type);
        if (key == null) {
            key = dimensionTypeToRegistryKey(DimensionType.getDefaultDimensionType());
            assert key != null;
        }

        return key == DimensionType.OVERWORLD_REGISTRY_KEY ? "Overworld"
                : key == DimensionType.THE_END_REGISTRY_KEY ? "The End"
                : key == DimensionType.THE_NETHER_REGISTRY_KEY ? "The Nether"
                : StringUtils.normalizeCapitalization(key.getValue().getPath());
    }

    @Nullable
    public static RegistryKey<DimensionType> dimensionTypeToRegistryKey(@NotNull final DimensionType type) {
        for (RegistryKey<DimensionType> registryKey : registryKeys) {
            DimensionType dim = KiloServer.getServer().getWorld(registryKey).getDimension();
            if (dim.equals(type)) {
                return registryKey;
            }
        }

        return null;
    }

    public static List<RegistryKey<DimensionType>> getDimensionKeys() {
        return registryKeys;
    }

    static {
        for (Identifier id : KiloServer.getServer().getMinecraftServer().method_29174().getRegistry().getIds()) {
            registryKeys.add(RegistryKey.of(Registry.DIMENSION_TYPE_KEY, id));
        }
    }

}
