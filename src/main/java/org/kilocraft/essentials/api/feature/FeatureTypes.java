package org.kilocraft.essentials.api.feature;

import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.extensions.homes.api.UserHomeStorage;
import org.kilocraft.essentials.extensions.warps.WarpManager;

/**
 * @deprecated Not implemented yet.
 */
public class FeatureTypes {
    public static void init() {}
    public static final FeatureType<UserHomeStorage> HOMES = KiloEssentials.getInstance().registerFeature(FeatureType.create(UserHomeStorage.class, "homes")); // TODO i509VCB: Reimpl homes to use a teleport helper within the UserHomeStorage in future
    public static final FeatureType<WarpManager> WARPS = KiloEssentials.getInstance().registerFeature(FeatureType.create(WarpManager.class, "warps")); // TODO Move to using Provider only.
    //public static final FeatureType<UserParticleManager> PARTICLES = KiloEssentials.getInstance().registerFeature(FeatureType.create(UserParticleManager.class, "particles")); // TODO Isolate particle handling.
}
