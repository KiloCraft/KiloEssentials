package org.kilocraft.essentials.craft.player;

import java.util.ArrayList;
import java.util.List;

public abstract class KiloPlayerManager {
    private static List<KiloPlayer> players = new ArrayList<>();
    private KiloPlayerSaveHandler saveHandler;

    public KiloPlayerManager() {
    }

}
