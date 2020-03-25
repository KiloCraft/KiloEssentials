package org.kilocraft.essentials.servermeta;

import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.ServerMetadata;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.server.Server;
import org.kilocraft.essentials.api.text.TextFormat;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.util.TPSTracker;
import org.kilocraft.essentials.util.monitor.SystemMonitor;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

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
            if (playerEntity.networkHandler == null) {
                continue;
            }


            Server server = KiloServer.getServer();
            PlayerListMeta.serverName = server.getName();
            PlayerListMeta.serverTps = TPSTracker.tps1.getShortAverage();
            PlayerListMeta.serverFormattedTps = "&" + TextFormat.getFormattedTPS(TPSTracker.tps1.getAverage()) + TPSTracker.tps1.getShortAverage() + "&r";
            PlayerListMeta.serverPlayerCount = String.valueOf(server.getPlayerManager().getCurrentPlayerCount());
            PlayerListMeta.serverMemoryMax = String.valueOf(SystemMonitor.getRamMaxMB());

            double memoryUsedPercentage = SystemMonitor.getRamUsedPercentage();
            PlayerListMeta.serverMemoryPercentage = String.valueOf(memoryUsedPercentage);
            PlayerListMeta.serverFormattedMemoryPercentage = TextFormat.getFormattedPercentage(memoryUsedPercentage, true) + memoryUsedPercentage + "&r";

            PlayerListMeta.provideFor(playerEntity);
        }
    }

    public void updateDisplayName(String name) {
        ServerPlayerEntity player = KiloServer.getServer().getPlayer(name);
        PlayerListS2CPacket playerListPacket = new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, player);

        if (player != null) {
            KiloServer.getServer().getPlayerManager().sendToAll(playerListPacket);
        }
    }

    public void onPlayerJoined(ServerPlayerEntity player) {
        PlayerListMeta.provideFor(player);
    }

    public final void setDescription(final Text description) throws IOException {
        this.metadata.setDescription(description);

        final Properties properties = new Properties();
        properties.load(new FileInputStream(KiloEssentials.getServerProperties().toFile()));
        properties.setProperty("motd", TextFormat.translate(description.asFormattedString()));
        properties.store(new FileOutputStream(KiloEssentials.getServerProperties().toFile()), "");
    }

    public final Text getDescription() {
        return this.metadata.getDescription();
    }

}
