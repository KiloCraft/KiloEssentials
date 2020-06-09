package org.kilocraft.essentials.mixin.events;

import com.mojang.authlib.GameProfile;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.MessageType;
import net.minecraft.server.BanEntry;
import net.minecraft.server.BannedIpEntry;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.PlayerManager;
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
import org.kilocraft.essentials.events.player.PlayerConnectEventImpl;
import org.kilocraft.essentials.events.player.PlayerConnectedEventImpl;
import org.kilocraft.essentials.extensions.homes.api.Home;
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
import java.util.UUID;

@Mixin(PlayerManager.class)
public abstract class MixinPlayerManager {

    @Shadow
    @Final
    private static Logger LOGGER;

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
    private void disconnectMessage(SocketAddress socketAddress, GameProfile gameProfile, CallbackInfoReturnable<Text> cir) {
        PlayerManager playerManager = KiloServer.getServer().getMinecraftServer().getPlayerManager();
        BannedPlayerEntry bannedPlayerEntry = playerManager.getUserBanList().get(gameProfile);
        BannedIpEntry bannedIpEntry = playerManager.getIpBanList().get(socketAddress);
        if (bannedPlayerEntry != null) {
            if (bannedPlayerEntry.getExpiryDate() != null) {
                cir.setReturnValue(new TextMessage(replaceVariables(KiloConfig.main().moderation().disconnectReasons().tempBan, bannedPlayerEntry)).toText());
            } else {
                cir.setReturnValue(new TextMessage(replaceVariables(KiloConfig.main().moderation().disconnectReasons().permBan, bannedPlayerEntry)).toText());
            }
        } else if(playerManager.getIpBanList().isBanned(socketAddress)) {
            if (bannedIpEntry.getExpiryDate() != null) {
                cir.setReturnValue(new TextMessage(replaceVariables(KiloConfig.main().moderation().disconnectReasons().tempIpBan, bannedIpEntry)).toText());
            } else {
                cir.setReturnValue(new TextMessage(replaceVariables(KiloConfig.main().moderation().disconnectReasons().permIpBan, bannedIpEntry)).toText());
            }
        } else if(!playerManager.isWhitelisted(gameProfile)) {
            cir.setReturnValue(new TextMessage("You are not white-listed on this server!").toText());
        } else {
            cir.setReturnValue(playerManager.getCurrentPlayerCount() >= playerManager.getMaxPlayerCount() && !playerManager.canBypassPlayerLimit(gameProfile) ? new TextMessage("The server is full!").toText() : null);
        }
    }

    public String replaceVariables(final String str, final BanEntry banEntry) {
        String string = new ConfigObjectReplacerUtil("ban", str, true)
                .append("reason", banEntry.getReason())
                .append("expiry", banEntry.getExpiryDate() == null ? "Error, please report to administrator" : banEntry.getExpiryDate().toString())
                .append("source", banEntry.getSource())
                .toString();
        return string;
    }


}
