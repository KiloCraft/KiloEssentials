package org.kilocraft.essentials.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.text.Text;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.user.OnlineUser;
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

	@Inject(method = "getDisplayName", at = @At(value = "TAIL", target = "Lnet/minecraft/network/packet/s2c/play/PlayerListS2CPacket$Entry;getDisplayName()Lnet/minecraft/text/Text;"), cancellable = true)
	private void modifyEntry(CallbackInfoReturnable<Text> cir) {
		boolean useNickname = KiloConfig.main().playerList().useNicknames;
		OnlineUser user = KiloServer.getServer().getOnlineUser(profile.getName());
		cir.setReturnValue(useNickname ? user.getRankedDisplayName() : user.getRankedName());
	}

}
