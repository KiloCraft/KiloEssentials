package org.kilocraft.essentials.mixin.events;

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
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {

    @Shadow
    public ServerPlayer player;

    @Shadow
    public abstract ServerPlayer getPlayer();

    @ModifyArg(
            method = "onDisconnect",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;broadcastMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V"
            ),
            index = 0
    )
    public Component modifyLeaveMessage(Component text) {
        return ComponentText.toText(ConfigVariableFactory.replaceUserVariables(ModConstants.translation("player.left"), KiloEssentials.getUserManager().getOnline(this.player)));
    }

    @Redirect(
            method = "onDisconnect",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;broadcastMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V"
            )
    )
    public void shouldBroadCastLeave(PlayerList playerManager, Component message, ChatType type, UUID sender) {
        if (!KiloEssentials.getUserManager().getOnline(this.player).getPreference(Preferences.VANISH)) playerManager.broadcastMessage(message, type, sender);
    }

    @Inject(at = @At(value = "RETURN"), method = "onDisconnect")
    private void onPlayerLeave(Component reason, CallbackInfo ci) {
        KiloEssentials.getUserManager().onLeave(this.player);
    }
}
