package org.kilocraft.essentials.user;

import com.mojang.authlib.GameProfile;

import net.minecraft.server.BannedIpEntry;
import net.minecraft.server.BannedIpList;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.BannedPlayerList;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.text.TextFormat;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.util.text.Texter;

import java.net.SocketAddress;
import java.util.Date;

public class PunishmentManager {
    PlayerManager playerManager;

    public PunishmentManager(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    public BannedPlayerList getProfileBanList() {
        return playerManager.getUserBanList();
    }

    public BannedIpList getIpBanList() {
        return playerManager.getIpBanList();
    }

    public void kick(ServerPlayerEntity player, String reason) {
        final String finalReason = reason == null ? KiloConfig.main().moderation().defaults().kick : reason;
        player.networkHandler.disconnect(Texter.newText(reason));
    }

    public void ban(GameProfile profile, String source, String reason) {
    	ban(profile, source, reason, null);
    }

    public void ban(GameProfile profile, String source, String reason, Date expireDate) {
        final String finalReason = reason == null ? KiloConfig.main().moderation().defaults().ban : reason;
    	final BannedPlayerEntry bannedPlayerEntry = new BannedPlayerEntry(profile, new Date(), source, expireDate, TextFormat.translate(reason));
    	getProfileBanList().add(bannedPlayerEntry);
    }

    public void ban(String source, String ip, String reason) {
    	ban(source, ip, reason, null);
    }
    
    public void ban(String source, String ip, String reason, Date expireDate) {
    	BannedIpEntry entry = new BannedIpEntry(ip, new Date(), source, expireDate, reason);
		getIpBanList().add(entry);
    }

    public void pardon(GameProfile profile) {
        if (getProfileBanList().contains(profile)) {
            getProfileBanList().remove(profile);
        }
    }

    public boolean shouldBan(GameProfile profile, String reason) {
        BannedPlayerEntry entry = getProfileBanList().get(profile);
        return isProfileBanned(profile) && entry != null && !entry.getReason().equals(TextFormat.translate(reason));
    }

    public boolean isProfileBanned(GameProfile profile) {
        return getProfileBanList().contains(profile);
    }

    public boolean isIpBanned(String ip) {
        return getIpBanList().isBanned(ip);
    }

    public boolean isIpBanned(SocketAddress address) {
        return getIpBanList().isBanned(address);
    }
}
