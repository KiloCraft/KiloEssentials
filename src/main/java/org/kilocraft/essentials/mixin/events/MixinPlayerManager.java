package org.kilocraft.essentials.mixin.events;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.MessageType;
import net.minecraft.server.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.chat.MutableTextMessage;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.config.main.sections.ModerationConfigSection;
import org.kilocraft.essentials.events.player.PlayerConnectEventImpl;
import org.kilocraft.essentials.events.player.PlayerConnectedEventImpl;
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

import static org.kilocraft.essentials.user.ServerUserManager.replaceVariables;

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
        try {
            String message = null;
            ModerationConfigSection.Messages messages = KiloConfig.main().moderation().messages();
            if (this.bannedProfiles.get(gameProfile) != null) {
                BannedPlayerEntry entry = this.bannedProfiles.get(gameProfile);
                assert entry != null;
                if (entry.getExpiryDate() == null) {
                    message = replaceVariables(messages.permBan, entry, true);
                } else {
                    message = replaceVariables(messages.tempBan, entry, false);
                }
            } else if (this.bannedIps.get(socketAddress) != null) {
                BannedIpEntry entry = this.bannedIps.get(socketAddress);
                assert entry != null;
                if (entry.getExpiryDate() == null) {
                    message = replaceVariables(messages.permIpBan, entry, true);
                } else {
                    message = replaceVariables(messages.tempIpBan, entry, false);
                }
            } else if (this.whitelistEnabled && !this.whitelist.isAllowed(gameProfile)) {
                if (messages.whitelist.isEmpty()) {
                    cir.setReturnValue(new TranslatableText("multiplayer.disconnect.not_whitelisted"));
                } else {
                    message = messages.whitelist;
                }
            }

            cir.setReturnValue(message == null ? null : ComponentText.toText(message));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
