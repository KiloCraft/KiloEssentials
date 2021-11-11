package org.kilocraft.essentials.mixin.patch.technical;

import net.minecraft.server.filter.TextStream;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
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

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    @Shadow
    public ServerPlayerEntity player;

    @Shadow
    public abstract void disconnect(Text reason);

    private float sentCharacters = 0;

    @Inject(
            method = "handleMessage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/filter/TextStream$Message;getFiltered()Ljava/lang/String;"
            ),
            cancellable = true
    )
    public void onChatMessage(TextStream.Message message, CallbackInfo ci) {
        final float KICK_THRESHOLD = 100;
        if (!KiloConfig.main().chat().useVanillaChat) {
            if (this.sentCharacters >= KICK_THRESHOLD && !KiloEssentials.hasPermissionNode(this.player.getCommandSource(), EssentialPermission.CHAT_BYPASS_SPAM)) {
                this.disconnect(new TranslatableText("disconnect.spam"));
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
    @Redirect(
            method = "onSignUpdate(Lnet/minecraft/network/packet/c2s/play/UpdateSignC2SPacket;Ljava/util/List;)V",
            at = @At(
                    value = "NEW",
                    target = "net/minecraft/text/LiteralText"
            )
    )
    public LiteralText useAdventureFormatting(String input) {
        if (!KiloEssentials.hasPermissionNode(this.player.getCommandSource(), EssentialPermission.SIGN_COLOR)) {
            input = ComponentText.clearFormatting(input);
        }
        return (LiteralText) ComponentText.toText(input);
    }

}
