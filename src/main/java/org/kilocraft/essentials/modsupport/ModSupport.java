package org.kilocraft.essentials.modsupport;

import net.fabricmc.loader.api.FabricLoader;
import org.kilocraft.essentials.api.SupportedMod;

import java.util.ArrayList;
import java.util.List;

public class ModSupport {
    private static List<SupportedMod> mods = new ArrayList<>();

    public ModSupport() {
    }

    public static void validateMods() {
        for (SupportedMod mod : mods) {
            if (mod.isFabricMod() && FabricLoader.getInstance().isModLoaded(mod.getModId()))
                mod.setPresent(true);
        }
    }

    public static void register(SupportedMod m) {
        mods.add(m);
    }

    public static List<SupportedMod> getSupportedMods() {
        return mods;
    }

    public static SupportedMod getMod(String id) {
        for (SupportedMod mod : mods) {
            if (mod.getModId().equals(id))
                return mod;
        }

        return null;
    }

}
