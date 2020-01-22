package org.kilocraft.essentials.extensions.betterchairs;

import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.feature.ConfigurableFeature;
import org.kilocraft.essentials.util.LocationImpl;

public class BetterChairs implements ConfigurableFeature {
    public static boolean enabled = false;

    @Override
    public boolean register() {
        enabled = true;
        return true;
    }

    public boolean sit(ServerPlayerEntity player, LocationImpl loc) {

        return false;
    }


}
