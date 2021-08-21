package org.kilocraft.essentials.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.text.Text;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.config.KiloConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListS2CPacket.Entry.class)
public abstract class PlayerListS2CPacketMixin {
	@Shadow @Final private GameProfile profile;

	@Inject(method = "getDisplayName", at = @At(value = "HEAD", target = "Lnet/minecraft/network/packet/s2c/play/PlayerListS2CPacket$Entry;getDisplayName()Lnet/minecraft/text/Text;"), cancellable = true)
	private void modifyPlayerListEntry(CallbackInfoReturnable<Text> cir) {
		if (KiloConfig.main().playerList().useNicknames) {
			Text text = KiloEssentials.getUserManager().getOnline(profile.getId()).getRankedDisplayName();
			cir.setReturnValue(text);
		}
	}

}
