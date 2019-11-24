package org.kilocraft.essentials.mixin.deprecated;

import com.google.common.collect.Lists;
import com.mojang.authlib.properties.Property;
import net.minecraft.client.network.packet.PlayerListS2CPacket;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.PacketByteBuf;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.user.ServerUser;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * // TODO i509VCB: Will this be implemented?
 * i509VCB: This is the cause of the packet errors. I think the client is disconnecting itself when this packet is sent due to somewhere in the packet a null value or invalid packet syntax is sent.
 * If you try to implement this in future use https://wiki.vg/Protocol#Player_Info to make sure your syntax is correct.
 *
 * @deprecated Not implemented yet.
 */
@Deprecated
@Mixin(PlayerListS2CPacket.class)
public class PlayerListPacketMixin {
	@Shadow
	private PlayerListS2CPacket.Action action;
	@Shadow
	private List<PlayerListS2CPacket.Entry> entries = Lists.newArrayList();

	public void write(PacketByteBuf packetByteBuf_1) throws IOException {
	      packetByteBuf_1.writeEnumConstant(this.action);
	      packetByteBuf_1.writeVarInt(this.entries.size());
	      Iterator var2 = this.entries.iterator();

	      while(true) {
	         while(var2.hasNext()) {
	            PlayerListS2CPacket.Entry playerListS2CPacket$Entry_1 = (PlayerListS2CPacket.Entry)var2.next();
	            ServerPlayerEntity player = KiloServer.getServer().getPlayerManager().getPlayer(playerListS2CPacket$Entry_1.getProfile().getId());

	            switch(this.action) {
	            case ADD_PLAYER:
	               packetByteBuf_1.writeUuid(playerListS2CPacket$Entry_1.getProfile().getId());
	               packetByteBuf_1.writeString(playerListS2CPacket$Entry_1.getProfile().getName());
	               packetByteBuf_1.writeVarInt(playerListS2CPacket$Entry_1.getProfile().getProperties().size());
	               Iterator var4 = playerListS2CPacket$Entry_1.getProfile().getProperties().values().iterator();
	               
	               while(var4.hasNext()) {
	                  Property property_1 = (Property)var4.next();
	                  packetByteBuf_1.writeString(property_1.getName());
	                  packetByteBuf_1.writeString(property_1.getValue());
	                  if (property_1.hasSignature()) {
	                     packetByteBuf_1.writeBoolean(true);
	                     packetByteBuf_1.writeString(property_1.getSignature());
	                  } else {
	                     packetByteBuf_1.writeBoolean(false);
	                  }
	               }

	               packetByteBuf_1.writeVarInt(playerListS2CPacket$Entry_1.getGameMode().getId());
	               packetByteBuf_1.writeVarInt(playerListS2CPacket$Entry_1.getLatency());
	               packetByteBuf_1.writeBoolean(true);
	               LiteralText displayText = new LiteralText(TextFormat.translateAlternateColorCodes('&',((Team) player.getScoreboardTeam()).getPrefix().asString()
							+ "&r " + KiloServer.getServer().getUserManager().getOnline(player).getPlayer().getDisplayName().asFormattedString()));
	               packetByteBuf_1.writeText(displayText);
	               break;
	            case UPDATE_GAME_MODE:
	               packetByteBuf_1.writeUuid(playerListS2CPacket$Entry_1.getProfile().getId());
	               packetByteBuf_1.writeVarInt(playerListS2CPacket$Entry_1.getGameMode().getId());
	               break;
	            case UPDATE_LATENCY:
	               packetByteBuf_1.writeUuid(playerListS2CPacket$Entry_1.getProfile().getId());
	               packetByteBuf_1.writeVarInt(playerListS2CPacket$Entry_1.getLatency());
	               break;
	            case UPDATE_DISPLAY_NAME:
	               packetByteBuf_1.writeUuid(playerListS2CPacket$Entry_1.getProfile().getId());
	               packetByteBuf_1.writeBoolean(true);
	               displayText = new LiteralText(TextFormat.translateAlternateColorCodes('&',((Team) player.getScoreboardTeam()).getPrefix().asString()
							+ "&r " + KiloServer.getServer().getUserManager().getOnline(player).getPlayer().getDisplayName().asFormattedString()));
	               packetByteBuf_1.writeText(displayText);
	               break;
	            case REMOVE_PLAYER:
	               packetByteBuf_1.writeUuid(playerListS2CPacket$Entry_1.getProfile().getId());
	            }
	         }

	         return;
	      }
	   }
}
