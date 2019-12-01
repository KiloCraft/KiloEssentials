package org.kilocraft.essentials.mixin;

import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.BannedIpEntry;
import net.minecraft.server.BannedIpList;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.BannedPlayerList;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
	
	@Shadow private BannedPlayerList bannedProfiles;
	@Shadow private BannedIpList bannedIps;
	@Shadow private List<ServerPlayerEntity> players = Lists.newArrayList();
	@Shadow protected int maxPlayers;
	@Shadow private static SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
	
	@Overwrite
	public Text checkCanJoin(SocketAddress socketAddress, GameProfile gameProfile) {
	    System.out.println("TEST");
		TranslatableText text2;
	      if (bannedProfiles.contains(gameProfile)) {
	    	 BannedPlayerEntry bannedPlayerEntry = (BannedPlayerEntry)this.bannedProfiles.get(gameProfile);
	    	 System.out.println(Date.from(Instant.now()));
	    	 System.out.println("E: " + bannedPlayerEntry.getExpiryDate());
	    	 if (bannedPlayerEntry.getExpiryDate() != null && Date.from(Instant.now()).compareTo(bannedPlayerEntry.getExpiryDate()) > 0) {
	    		 return null;
	    	 }
	    	   
	         text2 = new TranslatableText("multiplayer.disconnect.banned.reason", new Object[]{bannedPlayerEntry.getReason()});
	         if (bannedPlayerEntry.getExpiryDate() != null) {
	            text2.append((Text)(new TranslatableText("multiplayer.disconnect.banned.expiration", new Object[]{DATE_FORMATTER.format(bannedPlayerEntry.getExpiryDate())})));
	         }

	         return text2;
	      } else if (!((PlayerManager)(Object)(this)).isWhitelisted(gameProfile)) {
	         return new TranslatableText("multiplayer.disconnect.not_whitelisted", new Object[0]);
	      } else if (this.bannedIps.isBanned(socketAddress)) {
	         BannedIpEntry bannedIpEntry = this.bannedIps.get(socketAddress);
	         text2 = new TranslatableText("multiplayer.disconnect.banned_ip.reason", new Object[]{bannedIpEntry.getReason()});
	         if (bannedIpEntry.getExpiryDate() != null && Date.from(Instant.now()).compareTo(bannedIpEntry.getExpiryDate()) < 0) {
	            text2.append((Text)(new TranslatableText("multiplayer.disconnect.banned_ip.expiration", new Object[]{DATE_FORMATTER.format(bannedIpEntry.getExpiryDate())})));
	         }

	         return text2;
	      } else {
	         return players.size() >= maxPlayers && ((PlayerManager)(Object)(this)).canBypassPlayerLimit(gameProfile) ? new TranslatableText("multiplayer.disconnect.server_full", new Object[0]) : null;
	      }
	   }
}
