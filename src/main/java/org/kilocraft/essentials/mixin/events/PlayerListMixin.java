package org.kilocraft.essentials.mixin.events;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.IpBanList;
import net.minecraft.server.players.IpBanListEntry;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.UserBanList;
import net.minecraft.server.players.UserBanListEntry;
import net.minecraft.world.scores.PlayerTeam;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.config.ConfigVariableFactory;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.config.main.sections.ModerationConfigSection;
import org.kilocraft.essentials.events.PlayerEvents;
import org.kilocraft.essentials.user.preference.Preferences;
import org.objectweb.asm.Opcodes;
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
import java.util.Map;
import java.util.UUID;

import static org.kilocraft.essentials.user.ServerUserManager.replaceBanVariables;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {

    @Shadow
    @Final
    private UserBanList bans;
    @Shadow
    @Final
    private IpBanList ipBans;
    private ServerPlayer connectingPlayer;

    // TODO: Rework the ban system
    @Inject(
            method = "canPlayerLogin",
            at = @At("HEAD"),
            cancellable = true
    )
    private void checkCanJoin(SocketAddress socketAddress, GameProfile gameProfile, CallbackInfoReturnable<Component> cir) {
        String message = null;
        ModerationConfigSection.Messages messages = KiloConfig.main().moderation().messages();
        UserBanListEntry bannedPlayerEntry = this.bans.get(gameProfile);
        if (bannedPlayerEntry != null) {
            if (bannedPlayerEntry.getExpires() == null) {
                message = replaceBanVariables(messages.permBan, bannedPlayerEntry, true);
            } else {
                message = replaceBanVariables(messages.tempBan, bannedPlayerEntry, false);
            }
        } else if (this.ipBans.get(socketAddress) != null) {
            IpBanListEntry entry = this.ipBans.get(socketAddress);
            assert entry != null;
            if (entry.getExpires() == null) {
                message = replaceBanVariables(messages.permIpBan, entry, true);
            } else {
                message = replaceBanVariables(messages.tempIpBan, entry, false);
            }
        }

        if (message != null) cir.setReturnValue(ComponentText.toText(message));
    }

    // TODO: Move to separate mixin (not an event)
    @Redirect(
            method = "updateEntireScoreboard",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/ServerScoreboard;getPlayerTeams()Ljava/util/Collection;"
            )
    )
    public Collection<PlayerTeam> changeScoreboardPacket(ServerScoreboard serverScoreboard) {
        if (KiloConfig.main().playerList().customOrder) {
            return Collections.emptyList();
        } else {
            return serverScoreboard.getPlayerTeams();
        }
    }

    @ModifyArg(
            method = "placeNewPlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;broadcastMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V"
            ),
            index = 0
    )
    public Component modifyJoinMessage(Component text) {
        return ComponentText.toText(ConfigVariableFactory.replaceUserVariables(ModConstants.translation("player.joined"), KiloEssentials.getUserManager().getOnline(this.connectingPlayer)));
    }

    @Redirect(
            method = "placeNewPlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;broadcastMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V"
            )
    )
    public void shouldBroadCastJoin(PlayerList playerManager, Component message, ChatType type, UUID sender, Connection connection, ServerPlayer player) {
        if (!KiloEssentials.getUserManager().getOnline(player).getPreference(Preferences.VANISH))
            playerManager.broadcastMessage(message, type, sender);
    }

    @Inject(
            method = "placeNewPlayer",
            at = @At("HEAD")
    )
    public void acquireLocale(Connection connection, ServerPlayer player, CallbackInfo ci) {
        this.connectingPlayer = player;
    }

    @Inject(
            method = "placeNewPlayer",
            at = @At("TAIL")
    )
    public void onPlayerReady(Connection connection, ServerPlayer player, CallbackInfo ci) {
        PlayerEvents.PLAYER_READY.invoker().onPlayerReady(connection, player);
    }

}
