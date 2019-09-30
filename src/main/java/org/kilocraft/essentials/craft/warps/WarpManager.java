package org.kilocraft.essentials.craft.warps;

import com.electronwill.nightconfig.core.Config;
import org.kilocraft.essentials.craft.registry.ConfigurableFeature;

import java.util.ArrayList;

public class WarpManager implements ConfigurableFeature {
    //private static ArrayList<Config> warps = KiloConfig.getWarps().get("Warps");

    public static ArrayList<Config> getWarps() {
        return null;
    }


    @Override
    public boolean register() {
        System.out.println("Registering WarpManager");
        new WarpManager();
        return true;
    }
}
