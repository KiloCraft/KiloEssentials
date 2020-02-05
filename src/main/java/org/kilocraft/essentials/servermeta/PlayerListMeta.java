package org.kilocraft.essentials.servermeta;

import net.minecraft.client.network.packet.PlayerListHeaderS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.api.server.Server;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.mixin.accessor.PlayerListHeaderS2CPacketMixin;
import org.kilocraft.essentials.util.TPSTracker;

public class PlayerListMeta {
    static String header = "", footer = "";

    static void load() {
        header = KiloConfig.main().playerList().getHeader();
        footer = KiloConfig.main().playerList().getFooter();
    }

    static void provideFor(ServerPlayerEntity player) {
        if (player == null || player.networkHandler == null)
            return;

        PlayerListHeaderS2CPacket packet = new PlayerListHeaderS2CPacket();
        ((PlayerListHeaderS2CPacketMixin) packet).setHeader(TextFormat.translateToLiteralText('&', getFormattedStringFor(player, header)));
        ((PlayerListHeaderS2CPacketMixin) packet).setFooter(TextFormat.translateToLiteralText('&', getFormattedStringFor(player, footer)));

        player.networkHandler.sendPacket(packet);
    }

    private static String getFormattedStringFor(ServerPlayerEntity player, String string) {
        OnlineUser user = KiloServer.getServer().getOnlineUser(player);
        Server server = KiloServer.getServer();
        return string.replaceAll("%PLAYER_NAME%", player.getEntityName())
                .replaceAll("%PLAYER_DISPLAYNAME%", player.getDisplayName().asFormattedString())
                .replaceAll("%PLAYER_PING%", String.valueOf(player.pingMilliseconds))
                .replaceAll("%PLAYER_FORMATTED_PING%", TextFormat.getFormattedPing(player.pingMilliseconds))
                .replaceAll("%USER_NAME%", user.getUsername())
                .replaceAll("%USER_DISPLAYNAME%", user.getDisplayName())
                .replaceAll("%SERVER_NAME%", server.getName())
                .replaceAll("%SERVER_TPS%", TPSTracker.tps1.getShortAverage())
                .replaceAll("%SERVER_FORMATTED_TPS%", "&" + TextFormat.getFormattedTPS(TPSTracker.tps1.getAverage()) + TPSTracker.tps1.getShortAverage() + "&r")
                .replaceAll("%SERVER_PLAYER_COUNT%", String.valueOf(server.getPlayerManager().getCurrentPlayerCount()));
    }

}
