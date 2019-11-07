package org.kilocraft.essentials.craft.mixin;

import com.google.common.collect.Lists;
import com.mojang.authlib.properties.Property;
import net.minecraft.client.network.packet.PlayerListS2CPacket;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.PacketByteBuf;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.craft.user.User;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

@Mixin(PlayerListS2CPacket.class)
public class MixinPlayerListPacket {
	@Shadow
	private PlayerListS2CPacket.Action action;
	@Shadow
	private List<PlayerListS2CPacket.Entry> entries = Lists.newArrayList();

	public void write(PacketByteBuf byteBuf) throws IOException { // TODO We should move this to another class and just call that class
	      byteBuf.writeEnumConstant(this.action);
	      byteBuf.writeVarInt(this.entries.size());
	      Iterator iterator = this.entries.iterator();

	      while(true) {
	         while(iterator.hasNext()) {
	            PlayerListS2CPacket.Entry gameProfileEntry = (PlayerListS2CPacket.Entry)iterator.next();
	            ServerPlayerEntity player = KiloServer.getServer().getPlayerManager().getPlayer(gameProfileEntry.getProfile().getId());
	            User user = KiloServer.getServer().getUserManager().getUser(gameProfileEntry.getProfile().getId());
	            
	            switch(this.action) {
	            case ADD_PLAYER:
	               byteBuf.writeUuid(gameProfileEntry.getProfile().getId());
	               byteBuf.writeString(gameProfileEntry.getProfile().getName());
	               byteBuf.writeVarInt(gameProfileEntry.getProfile().getProperties().size());
	               Iterator properties = gameProfileEntry.getProfile().getProperties().values().iterator();
	               
	               while(properties.hasNext()) {
	                  Property property = (Property)properties.next();
	                  byteBuf.writeString(property.getName());
	                  byteBuf.writeString(property.getValue());
	                  if (property.hasSignature()) {
	                     byteBuf.writeBoolean(true);
	                     byteBuf.writeString(property.getSignature());
	                  } else {
	                     byteBuf.writeBoolean(false);
	                  }
	               }

	               byteBuf.writeVarInt(gameProfileEntry.getGameMode().getId());
	               byteBuf.writeVarInt(gameProfileEntry.getLatency());
	               byteBuf.writeBoolean(true);
	               LiteralText displayText = new LiteralText(TextFormat.translateAlternateColorCodes('&',((Team) player.getScoreboardTeam()).getPrefix().asString()
							+ "&r " + User.of(player).getDisplayNameAsString()));
	               byteBuf.writeText(displayText);
	               break;
	            case UPDATE_GAME_MODE:
	               byteBuf.writeUuid(gameProfileEntry.getProfile().getId());
	               byteBuf.writeVarInt(gameProfileEntry.getGameMode().getId());
	               break;
	            case UPDATE_LATENCY:
	               byteBuf.writeUuid(gameProfileEntry.getProfile().getId());
	               byteBuf.writeVarInt(gameProfileEntry.getLatency());
	               break;
	            case UPDATE_DISPLAY_NAME:
	               byteBuf.writeUuid(gameProfileEntry.getProfile().getId());
	               byteBuf.writeBoolean(true);
	               displayText = new LiteralText(TextFormat.translateAlternateColorCodes('&',((Team) player.getScoreboardTeam()).getPrefix().asString()
							+ "&r " + User.of(player).getDisplayNameAsString()));
	               byteBuf.writeText(displayText);
	               break;
	            case REMOVE_PLAYER:
	               byteBuf.writeUuid(gameProfileEntry.getProfile().getId());
	            }
	         }

	         return;
	      }
	   }
}
