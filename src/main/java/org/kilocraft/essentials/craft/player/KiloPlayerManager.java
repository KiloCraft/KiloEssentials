package org.kilocraft.essentials.craft.player;

import org.kilocraft.essentials.api.KiloServer;

public class KiloPlayerManager {
    public void saveAll() {
        KiloServer.getServer().getPlayerManager().getPlayerList().forEach((playerEntity) -> {
            KiloPlayer.saveData(KiloPlayer.get(playerEntity));
        });
    }
}
