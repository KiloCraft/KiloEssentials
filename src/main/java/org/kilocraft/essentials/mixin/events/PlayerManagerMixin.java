package org.kilocraft.essentials.mixin.events;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.MessageType;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.config.ConfigVariableFactory;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.config.main.sections.ModerationConfigSection;
import org.kilocraft.essentials.events.PlayerEvents;
import org.kilocraft.essentials.user.preference.Preferences;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import static org.kilocraft.essentials.user.ServerUserManager.replaceBanVariables;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {

    @Shadow
    @Final
    private BannedPlayerList bannedProfiles;

    @Shadow
    @Final
    private BannedIpList bannedIps;

    private ServerPlayerEntity lastJoined;

    @Inject(method = "checkCanJoin", at = @At(value = "HEAD"), cancellable = true)
    private void checkCanJoin(SocketAddress socketAddress, GameProfile gameProfile, CallbackInfoReturnable<Text> cir) {
        String message = null;
        ModerationConfigSection.Messages messages = KiloConfig.main().moderation().messages();
        BannedPlayerEntry bannedPlayerEntry = this.bannedProfiles.get(gameProfile);
        if (bannedPlayerEntry != null) {
            if (bannedPlayerEntry.getExpiryDate() == null) {
                message = replaceBanVariables(messages.permBan, bannedPlayerEntry, true);
            } else {
                message = replaceBanVariables(messages.tempBan, bannedPlayerEntry, false);
            }
        } else if (this.bannedIps.get(socketAddress) != null) {
            BannedIpEntry entry = this.bannedIps.get(socketAddress);
            if (entry.getExpiryDate() == null) {
                message = replaceBanVariables(messages.permIpBan, entry, true);
            } else {
                message = replaceBanVariables(messages.tempIpBan, entry, false);
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

    @ModifyArg(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcastChatMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/MessageType;Ljava/util/UUID;)V"), index = 0)
    public Text modifyJoinMessage(Text text) {
        return ComponentText.toText(ConfigVariableFactory.replaceUserVariables(ModConstants.translation("player.joined"), KiloEssentials.getUserManager().getOnline(this.lastJoined)));
    }

    @Redirect(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcastChatMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/MessageType;Ljava/util/UUID;)V"))
    public void shouldBroadCastJoin(PlayerManager playerManager, Text message, MessageType type, UUID sender) {
        if (!KiloEssentials.getUserManager().getOnline(this.lastJoined).getPreference(Preferences.VANISH)) playerManager.broadcastChatMessage(message, type, sender);
    }

    @Inject(
            method = "onPlayerConnect",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;getGameRules()Lnet/minecraft/world/GameRules;"
            )
    )
    public void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        PlayerEvents.JOINED.invoker().onJoin(connection, player);
        this.lastJoined = player;
    }

}
