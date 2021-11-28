package org.kilocraft.essentials.api.util;

import net.minecraft.world.phys.HitResult;

public interface EntityServerRayTraceable {

    HitResult rayTrace(double maxDistance, float aFloat, boolean passThoroughFluids);

}
