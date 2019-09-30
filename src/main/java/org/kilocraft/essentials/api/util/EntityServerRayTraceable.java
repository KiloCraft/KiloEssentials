package org.kilocraft.essentials.api.util;

import net.minecraft.util.hit.HitResult;

public interface EntityServerRayTraceable {
    // This is marked @Environment(EnvType.CLIENT) (not available in serverside code) so we need to re-implement it.
    HitResult rayTraceInServer(double double_1, float float_1, boolean boolean_1);
}
