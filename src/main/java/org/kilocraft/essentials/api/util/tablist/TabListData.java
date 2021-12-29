package org.kilocraft.essentials.api.util.tablist;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.server.level.ServerPlayer;
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

    private void updateTabHeaderFooter(@NotNull ServerPlayer player) {
        ClientboundTabListPacket packet = new ClientboundTabListPacket(
                ComponentText.toText(formatFor(player, KiloConfig.main().playerList().getHeader())),
                ComponentText.toText(formatFor(player, KiloConfig.main().playerList().getFooter()))
        );
        player.connection.send(packet);
    }

    private void updateTabHeaderFooterEveryone() {
        for (ServerPlayer player : KiloEssentials.getMinecraftServer().getPlayerList().getPlayers()) {
            this.updateTabHeaderFooter(player);
        }
    }

    private static String formatFor(@NotNull final ServerPlayer player, @NotNull final String string) {
        final OnlineUser user = KiloEssentials.getUserManager().getOnline(player);
        String s = ConfigVariableFactory.replaceServerVariables(string);
        return ConfigVariableFactory.replaceOnlineUserVariables(s, user);
    }

    public void onUpdate() {
        this.updateTabHeaderFooterEveryone();
        for (ServerPlayer player : KiloEssentials.getMinecraftServer().getPlayerList().getPlayers()) {
            this.sendPacketToAll(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.UPDATE_DISPLAY_NAME, player));
        }
    }

    public void onJoin(ServerPlayer player) {
        this.updateTabHeaderFooterEveryone();
    }

    public void onLeave(ServerPlayer player) {
        this.updateTabHeaderFooterEveryone();
    }


}

