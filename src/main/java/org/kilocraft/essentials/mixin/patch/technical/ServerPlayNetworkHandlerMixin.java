package org.kilocraft.essentials.mixin.patch.technical;

import net.minecraft.server.filter.TextStream;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
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

    @Inject(method = "handleMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/filter/TextStream$Message;getFiltered()Ljava/lang/String;"), cancellable = true)
    public void onChatMessage(TextStream.Message message, CallbackInfo ci) {
        if (!KiloConfig.main().chat().useVanillaChat) {
            KiloEssentials.getUserManager().onChatMessage(this.player, message);
            ci.cancel();
        }
    }

    // Allow adventure formatting on signs
    @Redirect(method = "onSignUpdate(Lnet/minecraft/network/packet/c2s/play/UpdateSignC2SPacket;Ljava/util/List;)V", at = @At(value = "NEW", target = "net/minecraft/text/LiteralText"))
    public LiteralText useAdventureFormatting(String input) {
        if (!KiloEssentials.hasPermissionNode(this.player.getCommandSource(), EssentialPermission.SIGN_COLOR)) {
            input = ComponentText.clearFormatting(input);
        }
        return (LiteralText) ComponentText.toText(input);
    }

}
