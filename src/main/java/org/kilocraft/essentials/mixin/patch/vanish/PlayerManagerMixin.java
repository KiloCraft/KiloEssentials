package org.kilocraft.essentials.mixin.patch.vanish;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
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

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {

    private ServerPlayerEntity connectingPlayer;

    @Inject(method = "onPlayerConnect", at = @At(value = "HEAD"))
    public void onPlayerConnect$acquireLocale(ClientConnection clientConnection, ServerPlayerEntity serverPlayerEntity, CallbackInfo ci) {
        this.connectingPlayer = serverPlayerEntity;
    }

    @Redirect(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendToAll(Lnet/minecraft/network/Packet;)V"))
    public void onlySendNonVanished(PlayerManager playerManager, Packet<?> packet) {
        OnlineUser newPlayer = KiloEssentials.getUserManager().getOnline(this.connectingPlayer);
        for (OnlineUser onlineUser : KiloEssentials.getUserManager().getOnlineUsersAsList()) {
            if (onlineUser.hasPermission(CommandPermission.VANISH) || !newPlayer.getPreference(Preferences.VANISH)) {
                onlineUser.asPlayer().networkHandler.sendPacket(packet);
            }
        }
    }

    @Redirect(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I"))
    public int onlySendNonVanished(List<ServerPlayerEntity> list) {
        for (ServerPlayerEntity player : list) {
            if (!KiloEssentials.getUserManager().getOnline(player).getPreference(Preferences.VANISH) || KiloEssentials.getUserManager().getOnline(this.connectingPlayer).hasPermission(CommandPermission.VANISH)) {
                this.connectingPlayer.networkHandler.sendPacket(new PlayerListS2CPacket(PlayerListS2CPacket.Action.ADD_PLAYER, player));
            }
        }
        // Stop vanilla implementation, by returning 0 for the player list size
        return 0;
    }

}
