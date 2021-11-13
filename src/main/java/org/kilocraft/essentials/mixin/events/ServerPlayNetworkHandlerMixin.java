package org.kilocraft.essentials.mixin.events;

import net.minecraft.network.MessageType;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.config.ConfigVariableFactory;
import org.kilocraft.essentials.events.PlayerEvents;
import org.kilocraft.essentials.user.preference.Preferences;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Shadow
    public abstract ServerPlayerEntity getPlayer();

    @ModifyArg(
            method = "onDisconnected",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/PlayerManager;broadcastChatMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/MessageType;Ljava/util/UUID;)V"
            ),
            index = 0
    )
    public Text modifyLeaveMessage(Text text) {
        return ComponentText.toText(ConfigVariableFactory.replaceUserVariables(ModConstants.translation("player.left"), KiloEssentials.getUserManager().getOnline(this.player)));
    }

    @Redirect(
            method = "onDisconnected",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/PlayerManager;broadcastChatMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/MessageType;Ljava/util/UUID;)V"
            )
    )
    public void shouldBroadCastLeave(PlayerManager playerManager, Text message, MessageType type, UUID sender) {
        if (!KiloEssentials.getUserManager().getOnline(this.player).getPreference(Preferences.VANISH)) playerManager.broadcastChatMessage(message, type, sender);
    }

    @Inject(at = @At(value = "RETURN"), method = "onDisconnected")
    private void onPlayerLeave(Text reason, CallbackInfo ci) {
        KiloEssentials.getUserManager().onLeave(this.player);
    }
}
