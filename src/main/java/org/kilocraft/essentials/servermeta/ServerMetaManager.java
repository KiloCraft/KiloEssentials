package org.kilocraft.essentials.servermeta;

import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.ServerMetadata;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.KiloServer;

public class ServerMetaManager {
    private final ServerMetadata metadata;

    public ServerMetaManager(ServerMetadata metadata) {
        this.metadata = metadata;
    }

    public void load() {
        PlayerListMeta.load();
    }

    public void updateAll() {
        KiloServer.getServer().getPlayerManager().getPlayerList().forEach(PlayerListMeta::update);
    }

    public void updateDisplayName(ServerPlayerEntity player) {
        if (player != null) {
            PlayerListS2CPacket packet = new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, player);
            KiloServer.getServer().sendGlobalPacket(packet);
        }
    }

    public void onPlayerJoined(ServerPlayerEntity player) {
        PlayerListMeta.update(player);
    }


}
