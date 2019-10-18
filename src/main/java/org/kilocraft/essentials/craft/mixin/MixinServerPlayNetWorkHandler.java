package org.kilocraft.essentials.craft.mixin;

import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.packet.ChatMessageC2SPacket;
import org.kilocraft.essentials.craft.chat.ChatMessage;
import org.kilocraft.essentials.craft.chat.KiloChat;
import org.kilocraft.essentials.craft.config.KiloConifg;
import org.kilocraft.essentials.craft.config.provided.localVariables.PlayerVariables;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class MixinServerPlayNetWorkHandler {
    @Shadow public ServerPlayerEntity player;

    @Inject(
            method = "onChatMessage", cancellable = true,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcastChatMessage(Lnet/minecraft/text/Text;Z)V")
    )

    private void modify(ChatMessageC2SPacket chatMessageC2SPacket_1, CallbackInfo ci) {
        ci.cancel();

        String string = KiloConifg.getProvider().getMessages().getLocalFormatter(
                true,
                "general.messageFormat",
                new PlayerVariables(player),
                chatMessageC2SPacket_1.getChatMessage()
        );

        ChatMessage message = new ChatMessage(
                string,
                Thimble.hasPermissionChildOrOp(player.getCommandSource(), "kiloessentials.chat.format", 3)
        );


        KiloChat.broadCast(player, message);
    }
}
