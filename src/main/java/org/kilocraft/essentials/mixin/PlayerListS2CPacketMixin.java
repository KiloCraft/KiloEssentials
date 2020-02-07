package org.kilocraft.essentials.mixin;

import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.util.PacketByteBuf;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.config.KiloConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerListS2CPacket.class)
public abstract class PlayerListS2CPacketMixin {

	@Deprecated
	@Redirect(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/s2c/play/PlayerListS2CPacket;write(Lnet/minecraft/util/PacketByteBuf;)V"))
	private void modify(PlayerListS2CPacket playerListS2CPacket, PacketByteBuf packetByteBuf) {
		boolean useNickname = KiloConfig.main().playerList().useNicknames;
		for (PlayerListS2CPacket.Entry entry : playerListS2CPacket.getEntries()) {
			OnlineUser user = KiloServer.getServer().getOnlineUser(entry.getProfile().getName());
			packetByteBuf.writeString(useNickname ? user.getRankedDisplayName().asFormattedString() : user.getPlayer().getDisplayName().asFormattedString());
		}
	}

}
