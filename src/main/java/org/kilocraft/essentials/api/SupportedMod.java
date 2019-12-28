package org.kilocraft.essentials.api;

public interface SupportedMod {
    String getPackage();

    String getModId();

    boolean isPresent();

    boolean isFabricMod();

    void setPresent(boolean set);
}
