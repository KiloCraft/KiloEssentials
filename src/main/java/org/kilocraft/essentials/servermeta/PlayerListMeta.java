package org.kilocraft.essentials.servermeta;

import net.minecraft.client.network.packet.PlayerListHeaderS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.config.provided.localVariables.PlayerConfigVariables;
import org.kilocraft.essentials.config.provided.localVariables.ServerConfigVariables;
import org.kilocraft.essentials.config.provided.localVariables.UserConfigVariables;
import org.kilocraft.essentials.mixin.accessor.PlayerListHeaderS2CPacketAccessor;
import org.kilocraft.essentials.user.ServerUser;

public class PlayerListMeta {
    static String header = "", footer = "";

    static void load() {
        header = KiloConfig.getProvider().getMain().getStringSafely("server.player_list.header", "");
        footer = KiloConfig.getProvider().getMain().getStringSafely("server.player_list.footer", "");
    }

    static void provideFor(ServerPlayerEntity player) {
        OnlineUser user = KiloServer.getServer().getOnlineUser(player);
        String thisHeader = KiloConfig.getProvider().getMain().getLocalReplacer().replace(header,
                new UserConfigVariables((ServerUser) KiloServer.getServer().getOnlineUser(player)),
                new ServerConfigVariables(), new PlayerConfigVariables(player))
                .replaceAll("%USER_DISPLAYNAME%", user.getRankedDisplayname().asFormattedString());


        String thisFooter = KiloConfig.getProvider().getMain().getLocalReplacer().replace(footer,
                new UserConfigVariables((ServerUser) KiloServer.getServer().getOnlineUser(player)),
                new ServerConfigVariables(), new PlayerConfigVariables(player))
                .replaceAll("%USER_DISPLAYNAME%", user.getRankedDisplayname().asFormattedString());

        PlayerListHeaderS2CPacket packet = new PlayerListHeaderS2CPacket();
        ((PlayerListHeaderS2CPacketAccessor) packet).setHeader(TextFormat.translateToLiteralText('&', thisHeader));
        ((PlayerListHeaderS2CPacketAccessor) packet).setFooter(TextFormat.translateToLiteralText('&', thisFooter));

        if (packet != null)
            player.networkHandler.sendPacket(packet);
    }

}
