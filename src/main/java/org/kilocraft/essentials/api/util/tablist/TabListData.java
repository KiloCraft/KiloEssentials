package org.kilocraft.essentials.api.util.tablist;

import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.PlayerListHeaderS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.config.ConfigVariableFactory;
import org.kilocraft.essentials.config.KiloConfig;

public class TabListData {

    protected static final Logger LOGGER = KiloEssentials.getLogger();
    protected final KiloEssentials kiloEssentials;

    public TabListData(KiloEssentials kiloEssentials) {
        this.kiloEssentials = kiloEssentials;
    }

    protected void sendPacketToAll(Packet<?> packet) {
        this.kiloEssentials.sendGlobalPacket(packet);
    }

    private void updateTabHeaderFooter(@NotNull ServerPlayerEntity player) {
        PlayerListHeaderS2CPacket packet = new PlayerListHeaderS2CPacket(
                ComponentText.toText(formatFor(player, KiloConfig.main().playerList().getHeader())),
                ComponentText.toText(formatFor(player, KiloConfig.main().playerList().getFooter()))
        );
        player.networkHandler.sendPacket(packet);
    }

    private void updateTabHeaderFooterEveryone() {
        for (ServerPlayerEntity player : KiloEssentials.getMinecraftServer().getPlayerManager().getPlayerList()) {
            this.updateTabHeaderFooter(player);
        }
    }

    private static String formatFor(@NotNull final ServerPlayerEntity player, @NotNull final String string) {
        final OnlineUser user = KiloEssentials.getUserManager().getOnline(player);
        String s = ConfigVariableFactory.replaceServerVariables(string);
        return ConfigVariableFactory.replaceOnlineUserVariables(s, user);
    }

    public void onUpdate() {
        this.updateTabHeaderFooterEveryone();
        for (ServerPlayerEntity player : KiloEssentials.getMinecraftServer().getPlayerManager().getPlayerList()) {
            this.sendPacketToAll(new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, player));
        }
    }

    public void onJoin(ServerPlayerEntity player) {
        this.updateTabHeaderFooterEveryone();
    }

    public void onLeave(ServerPlayerEntity player) {
        this.updateTabHeaderFooterEveryone();
    }


}

