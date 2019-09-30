package org.kilocraft.essentials.craft.warps;

import com.electronwill.nightconfig.core.Config;
import org.kilocraft.essentials.craft.config.KiloConfig;
import org.kilocraft.essentials.craft.registry.ConfigurableFeature;

import java.util.ArrayList;

public class WarpManager implements ConfigurableFeature {
    private static ArrayList<Config> warps = KiloConfig.getWarps().get("Warps");

    public static ArrayList<Config> getWarps() {
        return warps;
    }


    @Override
    public boolean register() {
        System.out.println("Registering WarpManager");
        new WarpManager();
        return true;
    }
}
