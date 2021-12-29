package org.kilocraft.essentials.mixin.patch.vanish;

import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.user.preference.Preferences;
import org.kilocraft.essentials.util.CommandPermission;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {

    @Redirect(
            method = "placeNewPlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"
            )
    )
    public void onlySendNonVanished(PlayerList playerManager, Packet<?> packet, Connection connection, ServerPlayer player) {
        OnlineUser newPlayer = KiloEssentials.getUserManager().getOnline(player);
        for (OnlineUser onlineUser : KiloEssentials.getUserManager().getOnlineUsersAsList()) {
            if (onlineUser.hasPermission(CommandPermission.VANISH) || !newPlayer.getPreference(Preferences.VANISH)) {
                onlineUser.asPlayer().connection.send(packet);
            }
        }
    }

    @Redirect(
            method = "placeNewPlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;size()I"
            )
    )
    public int onlySendNonVanished(List<ServerPlayer> list, Connection connection, ServerPlayer connectingPlayer) {
        for (ServerPlayer player : list) {
            if (!KiloEssentials.getUserManager().getOnline(player).getPreference(Preferences.VANISH) || KiloEssentials.getUserManager().getOnline(connectingPlayer).hasPermission(CommandPermission.VANISH)) {
                connectingPlayer.connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, player));
            }
        }
        // Stop vanilla implementation, by returning 0 for the player list size
        return 0;
    }

}
