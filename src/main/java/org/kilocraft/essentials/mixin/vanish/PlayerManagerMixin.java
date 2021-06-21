package org.kilocraft.essentials.mixin.vanish;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.user.preference.Preferences;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    private ServerPlayerEntity connectingPlayer;

    @Inject(method = "onPlayerConnect", at = @At(value = "HEAD"))
    public void onPlayerConnect$acquireLocale(ClientConnection clientConnection, ServerPlayerEntity serverPlayerEntity, CallbackInfo ci) {
        connectingPlayer = serverPlayerEntity;
    }

    @Redirect(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendToAll(Lnet/minecraft/network/Packet;)V"))
    public void onPlayerConnect(PlayerManager playerManager, Packet<?> packet) {
        OnlineUser newPlayer = KiloEssentials.getServer().getUserManager().getOnline(connectingPlayer);
        for (OnlineUser onlineUser : KiloEssentials.getServer().getUserManager().getOnlineUsersAsList()) {
            if (onlineUser.hasPermission(CommandPermission.VANISH) || !newPlayer.getPreference(Preferences.VANISH)) {
                onlineUser.asPlayer().networkHandler.sendPacket(packet);
            }
        }
    }

    @Redirect(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I"))
    public int onPlayerConnect$onlySendNonVanished(List<ServerPlayerEntity> list) {
        for (ServerPlayerEntity player : list) {
            if (!KiloEssentials.getServer().getUserManager().getOnline(player).getPreference(Preferences.VANISH) || KiloEssentials.getServer().getUserManager().getOnline(connectingPlayer).hasPermission(CommandPermission.VANISH)) {
                connectingPlayer.networkHandler.sendPacket(new PlayerListS2CPacket(PlayerListS2CPacket.Action.ADD_PLAYER, player));
            }
        }
        return 0;
    }

}
