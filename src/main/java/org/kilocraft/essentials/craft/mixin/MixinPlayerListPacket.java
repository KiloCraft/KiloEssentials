package org.kilocraft.essentials.craft.mixin;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.craft.user.User;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import com.google.common.collect.Lists;
import com.mojang.authlib.properties.Property;
import net.minecraft.client.network.packet.PlayerListS2CPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.LiteralText;
import net.minecraft.util.PacketByteBuf;

@Mixin(PlayerListS2CPacket.class)
public class MixinPlayerListPacket {
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
	            PlayerEntity player = KiloServer.getServer().getPlayerManager().getPlayer(playerListS2CPacket$Entry_1.getProfile().getId());
	            User user = KiloServer.getServer().getUserManager().getUser(playerListS2CPacket$Entry_1.getProfile().getId());
	            
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
	               packetByteBuf_1.writeText(new LiteralText(((Team) player.getScoreboardTeam()).getPrefix().asString()
							+ " " + KiloServer.getServer().getUserManager().getUserDisplayName(player.getName().asString())));
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
	               packetByteBuf_1.writeText(new LiteralText(((Team) player.getScoreboardTeam()).getPrefix().asString()
							+ " " + KiloServer.getServer().getUserManager().getUserDisplayName(player.getName().asString())));
	               break;
	            case REMOVE_PLAYER:
	               packetByteBuf_1.writeUuid(playerListS2CPacket$Entry_1.getProfile().getId());
	            }
	         }

	         return;
	      }
	   }
}
