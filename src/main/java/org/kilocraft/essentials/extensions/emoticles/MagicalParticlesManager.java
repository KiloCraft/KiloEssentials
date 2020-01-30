package org.kilocraft.essentials.extensions.emoticles;

import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.feature.ConfigurableFeature;

public class MagicalParticlesManager implements ConfigurableFeature {
    public static boolean enabled = false;

    @Override
    public boolean register() {
        enabled = true;
        KiloEssentials.getInstance().getCommandHandler().register(new MagicalparticlesCommand());


        return true;
    }

}
