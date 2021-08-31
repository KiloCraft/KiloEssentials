package org.kilocraft.essentials.mixin.events;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.MessageType;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.config.main.sections.ModerationConfigSection;
import org.kilocraft.essentials.events.PlayerEvents;
import org.kilocraft.essentials.user.OnlineServerUser;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import static org.kilocraft.essentials.user.ServerUserManager.replaceVariables;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {

    @Shadow
    @Final
    private BannedPlayerList bannedProfiles;

    @Shadow
    @Final
    private BannedIpList bannedIps;

    /**
     * Moved to {@link org.kilocraft.essentials.chat.KiloChat#onUserJoin(OnlineServerUser)}
     */
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcastChatMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/MessageType;Ljava/util/UUID;)V"), method = "onPlayerConnect")
    private void cancelJoinMessage(PlayerManager playerManager, Text text, MessageType messageType, UUID uUID) {
    }

    @Redirect(at = @At(value = "INVOKE",
            target = "Lorg/apache/logging/log4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V"),
            method = "onPlayerConnect")
    private void cancelLogMessage(Logger logger, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        // noop
    }

    @Inject(method = "checkCanJoin", at = @At(value = "HEAD"), cancellable = true)
    private void override$checkCanJoin(SocketAddress socketAddress, GameProfile gameProfile, CallbackInfoReturnable<Text> cir) {
        String message = null;
        ModerationConfigSection.Messages messages = KiloConfig.main().moderation().messages();
        BannedPlayerEntry bannedPlayerEntry = this.bannedProfiles.get(gameProfile);
        if (bannedPlayerEntry != null) {
            if (bannedPlayerEntry.getExpiryDate() == null) {
                message = replaceVariables(messages.permBan, bannedPlayerEntry, true);
            } else {
                message = replaceVariables(messages.tempBan, bannedPlayerEntry, false);
            }
        } else if (this.bannedIps.get(socketAddress) != null) {
            BannedIpEntry entry = this.bannedIps.get(socketAddress);
            if (entry.getExpiryDate() == null) {
                message = replaceVariables(messages.permIpBan, entry, true);
            } else {
                message = replaceVariables(messages.tempIpBan, entry, false);
            }
        }

        if (message != null) cir.setReturnValue(ComponentText.toText(message));
    }


    @Redirect(method = "sendScoreboard", at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/ServerScoreboard;getTeams()Ljava/util/Collection;"))
    public Collection<Team> changeScoreboardPacket(ServerScoreboard serverScoreboard) {
        if (KiloConfig.main().playerList().customOrder) {
            return Collections.emptyList();
        } else {
            return serverScoreboard.getTeams();
        }
    }

    @Inject(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendToAll(Lnet/minecraft/network/Packet;)V"))
    public void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        PlayerEvents.JOINED.invoker().onJoin(connection, player);
    }

}
