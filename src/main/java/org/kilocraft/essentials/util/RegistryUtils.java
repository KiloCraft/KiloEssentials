package org.kilocraft.essentials.util;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;

public class RegistryUtils {
    public static Identifier toIdentifier(DimensionType type) {
        return Registry.DIMENSION_TYPE.getId(type);
    }

    public static DimensionType toDimension(Identifier identifier) {
        return Registry.DIMENSION_TYPE.get(identifier);
    }

}
