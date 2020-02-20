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

    public static String dimensionToName(Identifier identifier) {
        return dimensionToName(toDimension(identifier));
    }

    public static String dimensionToName(DimensionType type) {
        return type == DimensionType.OVERWORLD ? "Overworld" : type == DimensionType.THE_NETHER ? "The Nehter" : "The End";
    }

}
