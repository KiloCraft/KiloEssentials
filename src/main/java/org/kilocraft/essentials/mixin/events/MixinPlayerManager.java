package org.kilocraft.essentials.mixin.events;

import com.mojang.authlib.GameProfile;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.MessageType;
import net.minecraft.server.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.chat.TextMessage;
import org.kilocraft.essentials.config.ConfigObjectReplacerUtil;
import org.kilocraft.essentials.config.ConfigVariableFactory;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.config.main.sections.ModerationConfigSection;
import org.kilocraft.essentials.events.player.PlayerConnectEventImpl;
import org.kilocraft.essentials.events.player.PlayerConnectedEventImpl;
import org.kilocraft.essentials.extensions.homes.api.Home;
import org.kilocraft.essentials.util.TimeDifferenceUtil;
import org.kilocraft.essentials.util.text.Texter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.SocketAddress;
import java.util.Date;
import java.util.UUID;

@Mixin(PlayerManager.class)
public abstract class MixinPlayerManager {

    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    @Final
    private BannedPlayerList bannedProfiles;

    @Shadow
    @Final
    private BannedIpList bannedIps;

    @Shadow
    private boolean whitelistEnabled;

    @Shadow
    @Final
    private Whitelist whitelist;

    @Inject(at = @At("HEAD"), method = "onPlayerConnect", cancellable = true)
    private void oky$onPlayerConnect(ClientConnection connection, ServerPlayerEntity playerEntity, CallbackInfo ci) {
        PlayerConnectEventImpl e = KiloServer.getServer().triggerEvent(new PlayerConnectEventImpl(connection, playerEntity));
        LOGGER.info(playerEntity.getEntityName() + " with a Entity ID of \"" + playerEntity.getEntityId() + "\" Joined the server");

        if (e.isCancelled()) {
            connection.disconnect(new LiteralText(e.getCancelReason()));
            ci.cancel();
        }
    }


    @Inject(at = @At("TAIL"), method = "onPlayerConnect")
    private void onPlayerConnected(ClientConnection connection, ServerPlayerEntity playerEntity, CallbackInfo ci) {
        PlayerConnectedEventImpl e = KiloServer.getServer().triggerEvent(new PlayerConnectedEventImpl(connection, playerEntity));
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcastChatMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/MessageType;Ljava/util/UUID;)V"), method = "onPlayerConnect")
    private void oky$onPlayerConnect$sendToAll(PlayerManager playerManager, Text text, MessageType messageType, UUID uUID) {
        //Ignored
    }

    @Redirect(at = @At(value = "INVOKE",
            target = "Lorg/apache/logging/log4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V"),
            method = "onPlayerConnect")
    private void oky$onPlayerConnect$sendToAll(Logger logger, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        //Ignored
    }

    @Inject(method = "checkCanJoin", at = @At(value = "HEAD"), cancellable = true)
    private void override$checkCanJoin(SocketAddress socketAddress, GameProfile gameProfile, CallbackInfoReturnable<Text> cir) {
        TextMessage message = null;
        ModerationConfigSection.DisconnectReasons disconnectReasons = KiloConfig.main().moderation().disconnectReasons();
        if (this.bannedProfiles.contains(gameProfile)) {
            BannedPlayerEntry entry = this.bannedProfiles.get(gameProfile);
            assert entry != null;
            if (entry.getExpiryDate() == null) {
                message = new TextMessage(replaceVariables(disconnectReasons.permBan, entry, true));
            } else {
                message = new TextMessage(replaceVariables(disconnectReasons.tempBan, entry, false));
            }
        } else if (this.bannedIps.isBanned(socketAddress)) {
            BannedIpEntry entry = this.bannedIps.get(socketAddress);
            assert entry != null;
            if (entry.getExpiryDate() == null) {
                message = new TextMessage(replaceVariables(disconnectReasons.permIpBan, entry, true));
            } else {
                message = new TextMessage(replaceVariables(disconnectReasons.tempIpBan, entry, false));
            }
        } else if (this.whitelistEnabled && !this.whitelist.isAllowed(gameProfile)) {
            if (disconnectReasons.whitelist.isEmpty()) {
                cir.setReturnValue(new TranslatableText("multiplayer.disconnect.not_whitelisted"));
            } else {
                message = new TextMessage(disconnectReasons.whitelist);
            }
        }

        if (message != null) {
            cir.setReturnValue(message.toText());
        }
    }

    public String replaceVariables(final String str, final BanEntry<?> entry, final boolean permanent) {
        ConfigObjectReplacerUtil replacer = new ConfigObjectReplacerUtil("ban", str, true)
                .append("reason", entry.getReason())
                .append("source", entry.getSource());

        if (!permanent) {
            replacer.append("expiry", entry.getExpiryDate().toString())
                    .append("left", TimeDifferenceUtil.formatDateDiff(new Date(), entry.getExpiryDate()));
        }

        return replacer.toString();
    }


}
