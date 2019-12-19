package org.kilocraft.essentials.modsupport;

import org.kilocraft.essentials.api.SupportedMod;

import java.util.UUID;

public class VanishModSupport implements SupportedMod {
    private static boolean present = false;

    @Override
    public String getPackage() {
        return "io.github.indicode.fabric.vanish";
    }

    @Override
    public String getModId() {
        return "vanish";
    }

    @Override
    public boolean isPresent() {
        return present;
    }

    @Override
    public boolean isFabricMod() {
        return true;
    }

    @Override
    public void setPresent(boolean set) {
        present = set;
    }

    public static boolean isVanished(UUID uuid) {
        return false;
    }

}
