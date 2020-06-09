package org.kilocraft.essentials.servermeta;

import net.minecraft.network.packet.s2c.play.PlayerListHeaderS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.text.TextFormat;
import org.kilocraft.essentials.api.server.Server;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.mixin.accessor.PlayerListHeaderS2CPacketMixin;
import org.kilocraft.essentials.util.TPSTracker;
import org.kilocraft.essentials.util.monitor.SystemMonitor;
import org.kilocraft.essentials.util.text.Texter;

public class PlayerListMeta {
    static String header = "", footer = "";
    static String serverName = "";
    static String serverTps = "";
    static String serverFormattedTps = "";
    static String serverPlayerCount = "";
    static String serverMemoryMax = "";
    static String serverMemoryPercentage = "";
    static String serverFormattedMemoryPercentage = "";
    static String serverMemoryUsageMB = "";

    static void load() {
        header = KiloConfig.main().playerList().getHeader();
        footer = KiloConfig.main().playerList().getFooter();
    }

    static void provideFor(ServerPlayerEntity player) {
        if (player == null || player.networkHandler == null) {
            return;
        }

        PlayerListHeaderS2CPacket packet = new PlayerListHeaderS2CPacket();
        ((PlayerListHeaderS2CPacketMixin) packet).setHeader(TextFormat.translateToLiteralText('&', getFormattedStringFor(player, header)));
        ((PlayerListHeaderS2CPacketMixin) packet).setFooter(TextFormat.translateToLiteralText('&', getFormattedStringFor(player, footer)));

        player.networkHandler.sendPacket(packet);
    }

    private static String getFormattedStringFor(ServerPlayerEntity player, String string) {
        OnlineUser user = KiloServer.getServer().getOnlineUser(player);
        return string.replaceAll("%PLAYER_NAME%", player.getEntityName())
                .replaceAll("%PLAYER_DISPLAYNAME%", Texter.Legacy.toFormattedString(player.getDisplayName()))
                .replaceAll("%PLAYER_PING%", String.valueOf(player.pingMilliseconds))
                .replaceAll("%PLAYER_FORMATTED_PING%", TextFormat.getFormattedPing(player.pingMilliseconds))
                .replaceAll("%USER_DISPLAYNAME%", user.getFormattedDisplayName())
                .replaceAll("%SERVER_NAME%", serverName)
                .replaceAll("%SERVER_TPS%", serverTps)
                .replaceAll("%SERVER_FORMATTED_TPS%", serverFormattedTps)
                .replaceAll("%SERVER_PLAYER_COUNT%", serverPlayerCount)
                .replaceAll("%SERVER_MEMORY_MAX%", serverMemoryMax)
                .replaceAll("%SERVER_MEMORY_USAGE_PERCENTAGE%", serverMemoryPercentage)
                .replaceAll("%SERVER_FORMATTED_MEMORY_USAGE_PERCENTAGE%", serverFormattedMemoryPercentage)
                .replaceAll("%SERVER_MEMORY_USAGE_MB%", serverMemoryUsageMB);
    }

}
