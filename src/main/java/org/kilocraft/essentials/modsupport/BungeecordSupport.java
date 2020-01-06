package org.kilocraft.essentials.modsupport;

import org.kilocraft.essentials.api.SupportedMod;
import org.kilocraft.essentials.config.KiloConfig;

public class BungeecordSupport implements SupportedMod {
    private boolean present = KiloConfig.getProvider().getMain().getBooleanSafely("bungeecord-mode", false);

    @Override
    public String getPackage() {
        return "";
    }

    @Override
    public String getModId() {
        return "bungeecord";
    }

    @Override
    public boolean isPresent() {
        return present;
    }

    @Override
    public boolean isFabricMod() {
        return false;
    }

    @Override
    public void setPresent(boolean set) {
        this.present = set;
    }

}
