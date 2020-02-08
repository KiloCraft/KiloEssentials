package org.kilocraft.essentials.servermeta;

import net.minecraft.server.ServerMetadata;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.KiloServer;

public class ServerMetaManager {
    private ServerMetadata metadata;

    public ServerMetaManager(ServerMetadata metadata) {
        this.metadata = metadata;
    }

    public void load() {
        PlayerListMeta.load();
    }

    public void updateAll() {
        for (ServerPlayerEntity playerEntity : KiloServer.getServer().getPlayerManager().getPlayerList()) {
            if (playerEntity.networkHandler == null)
                continue;

            PlayerListMeta.provideFor(playerEntity);
        }
    }

    public void onPlayerJoined(ServerPlayerEntity player) {
        PlayerListMeta.provideFor(player);
    }

}
