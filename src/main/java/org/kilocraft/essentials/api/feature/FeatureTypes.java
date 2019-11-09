package org.kilocraft.essentials.api.feature;

import org.kilocraft.essentials.KiloEssentialsImpl;
import org.kilocraft.essentials.extensions.warps.WarpManager;

public class FeatureTypes {
    public static void init() {}
    //public static final FeatureType<UserHomeStorage> HOMES = KiloEssentials.getInstance().registerFeature(FeatureType.create(UserHomeStorage.class, "homes")); // TODO i509VCB: Reimpl homes to use a teleport helper within the UserHomeStorage in future
    public static final FeatureType<WarpManager> WARPS = KiloEssentialsImpl.getInstance().registerFeature(FeatureType.create(WarpManager.class, "warps"));
}
