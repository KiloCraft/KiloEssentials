package org.kilocraft.essentials.servermeta;

import net.minecraft.network.packet.s2c.play.PlayerListHeaderS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.config.ConfigVariableFactory;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.mixin.accessor.PlayerListHeaderS2CPacketMixin;

public class PlayerListMeta {
    private static String header = "";
    private static String footer = "";

    static void load() {
        header = KiloConfig.main().playerList().getHeader();
        footer = KiloConfig.main().playerList().getFooter();
    }

    static void update(ServerPlayerEntity player) {
        if (player == null || player.networkHandler == null) {
            return;
        }

        PlayerListHeaderS2CPacketMixin packet = (PlayerListHeaderS2CPacketMixin) new PlayerListHeaderS2CPacket();
        packet.setHeader(ComponentText.toText(ComponentText.of(formatFor(player, header), false)));
        packet.setFooter(ComponentText.toText(ComponentText.of(formatFor(player, footer), false)));

        player.networkHandler.sendPacket(packet);
    }

    private static String formatFor(@NotNull final ServerPlayerEntity player, @NotNull final String string) {
        final OnlineUser user = KiloServer.getServer().getOnlineUser(player);
        String s = ConfigVariableFactory.replaceServerVariables(string);
        return ConfigVariableFactory.replaceOnlineUserVariables(s, user);
    }

}
