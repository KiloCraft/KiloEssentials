package org.kilocraft.essentials.mixin.patch.technical;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.TextFilter;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.util.EssentialPermission;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
    @Shadow
    public ServerPlayer player;

    @Shadow
    public abstract void disconnect(Component reason);

    private float sentCharacters = 0;

    @Inject(
            method = "handleChat(Lnet/minecraft/server/network/TextFilter$FilteredText;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/TextFilter$FilteredText;getFiltered()Ljava/lang/String;"
            ),
            cancellable = true
    )
    public void onChatMessage(TextFilter.FilteredText message, CallbackInfo ci) {
        final float KICK_THRESHOLD = 100;
        if (!KiloConfig.main().chat().useVanillaChat) {
            if (this.sentCharacters >= KICK_THRESHOLD && !KiloEssentials.hasPermissionNode(this.player.createCommandSourceStack(), EssentialPermission.CHAT_BYPASS_SPAM)) {
                this.disconnect(new TranslatableComponent("disconnect.spam"));
            } else {
                KiloEssentials.getUserManager().onChatMessage(this.player, message);
                this.sentCharacters += message.getRaw().length();
            }
            ci.cancel();
        }
    }

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    public void onTick(CallbackInfo ci) {
        // Maximum allowed characters per second
        final float MAX_CPS = 12;
        this.sentCharacters = Math.max(this.sentCharacters - (MAX_CPS / 20), 0);
    }

    // Allow adventure formatting on signs
    // TODO:
    /*@Redirect(
            method = "updateSignText",
            at = @At(
                    value = "NEW",
                    target = "Lnet/minecraft/network/chat/TextComponent;<init>(Ljava/lang/String;)V"
            )
    )
    public TextComponent useAdventureFormatting(String input) {
        if (!KiloEssentials.hasPermissionNode(this.player.createCommandSourceStack(), EssentialPermission.SIGN_COLOR)) {
            input = ComponentText.clearFormatting(input);
        }
        return (TextComponent) ComponentText.toText(input);
    }*/

}
