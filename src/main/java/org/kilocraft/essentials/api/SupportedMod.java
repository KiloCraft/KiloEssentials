package org.kilocraft.essentials.api;

public interface SupportedMod {
    String getPackage();

    String getModId();

    boolean isPresent();

    void setPresent(boolean set);
}
