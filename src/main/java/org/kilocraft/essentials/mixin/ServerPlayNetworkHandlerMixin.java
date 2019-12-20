package org.kilocraft.essentials.mixin;

import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.packet.ChatMessageC2SPacket;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.chat.channels.GlobalChat;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.user.ServerUserManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    @Shadow public ServerPlayerEntity player;

    @Inject(method = "onChatMessage", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcastChatMessage(Lnet/minecraft/text/Text;Z)V"))
    private void modify(ChatMessageC2SPacket chatMessageC2SPacket_1, CallbackInfo ci) {
        if (KiloConfig.getProvider().getMain().getBooleanSafely("chat.use-vanilla-chat-instead", false) &&
                KiloServer.getServer().getOnlineUser(player).getUpstreamChannelId().equals(GlobalChat.getChannelId())) {
            return;
        }

        ci.cancel();
        ((ServerUserManager) KiloServer.getServer().getUserManager()).onChatMessage(player, chatMessageC2SPacket_1);
    }
}
