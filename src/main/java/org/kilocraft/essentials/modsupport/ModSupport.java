package org.kilocraft.essentials.modsupport;

import net.fabricmc.loader.api.FabricLoader;
import org.kilocraft.essentials.api.SupportedMod;

import java.util.List;

public class ModSupport <M extends SupportedMod> {
    private List<M> mods;

    public void validateMods() {
        for (M mod : mods) {
            if (FabricLoader.getInstance().isModLoaded(mod.getModId()))
                mod.setPresent(true);
        }
    }

    public void register(M m) {
        mods.add(m);
    }

    public List<M> getSupportedMods() {
        return mods;
    }

}
