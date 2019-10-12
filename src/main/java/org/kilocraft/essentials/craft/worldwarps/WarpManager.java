package org.kilocraft.essentials.craft.worldwarps;

import com.electronwill.nightconfig.core.Config;
import net.minecraft.util.math.BlockPos;
import org.kilocraft.essentials.craft.config.KiloConifg;
import org.kilocraft.essentials.craft.registry.ConfigurableFeature;

import java.util.ArrayList;

public class WarpManager implements ConfigurableFeature {
    public static WarpManager INSTANCE;
    private static ArrayList<Config> arrayList = KiloConifg.getWarps().get("warps");
    private static ArrayList<Warp> warps = new ArrayList<>();

    @Override
    public boolean register() {
        constructWarps();
        return true;
    }

    private void constructWarps() {
        arrayList.forEach((c) -> {
            Warp warp = new Warp(
                    c.toString(),
                    new BlockPos(
                            c.getIntOrElse("pos.x", 0),
                            c.getIntOrElse("pos.y", 0),
                            c.getIntOrElse("pos.z", 0)
                    ),
                    c.getOrElse("requires_permission", false)
            );

            warps.add(warp);
        });

    }

    public ArrayList<Warp> getWarps() {
        return warps;
    }

    public void addWarp(Warp warp) {
        warps.add(warp);
    }


}
