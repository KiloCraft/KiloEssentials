package org.kilocraft.essentials.user;

import com.mojang.authlib.GameProfile;

import net.minecraft.server.BannedIpEntry;
import net.minecraft.server.BannedIpList;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.BannedPlayerList;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

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

    public void kick(ServerPlayerEntity player, Text reason) {
        player.networkHandler.disconnect(reason);
    }

    public void ban(GameProfile profile, String source, String reason) {
    	ban(profile, source, reason, null);
    }

    public void ban(GameProfile profile, String source, String reason, Date expireDate) {
    	BannedPlayerEntry bannedPlayerEntry = new BannedPlayerEntry(profile, new Date(), source, expireDate, reason);
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
