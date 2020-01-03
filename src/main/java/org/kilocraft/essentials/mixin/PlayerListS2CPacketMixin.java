package org.kilocraft.essentials.mixin;

import net.minecraft.client.network.packet.PlayerListS2CPacket;
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
	@Redirect(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/PacketByteBuf;writeString(Ljava/lang/String;)Lnet/minecraft/util/PacketByteBuf;"))
	private PacketByteBuf modify(PacketByteBuf packetByteBuf, String string) {
		boolean useNickname = KiloConfig.getProvider().getMain().getBooleanSafely("server.player_list.use_nicknames", true);
		OnlineUser user = KiloServer.getServer().getOnlineUser(string);
		packetByteBuf.writeString(useNickname ? user.getRankedDisplayname().asFormattedString() : user.getPlayer().getDisplayName().asFormattedString());
		return packetByteBuf;
	}

}
