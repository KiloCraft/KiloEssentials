package org.kilocraft.essentials.user.punishment;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.BannedIpList;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.BannedPlayerList;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Date;

import static org.kilocraft.essentials.user.punishment.BanEntryType.*;

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

    public void ban(GameProfile profile, BanEntryType type) {
        if (type.equals(PROFILE)) {

        } else {

        }
    }


    public void ban(GameProfile profile, BanEntryType type, String reason) {
    	ban (profile, type, reason, null);
    }

    public void ban(GameProfile profile, BanEntryType type, String reason, Date expireDate) {
    	BannedPlayerList bannedPlayerList = playerManager.getUserBanList();
    	BannedPlayerEntry bannedPlayerEntry = new BannedPlayerEntry(profile, new Date(), null, expireDate, reason);
        bannedPlayerList.add(bannedPlayerEntry);
    }

    public void pardon(GameProfile profile, BanEntryType type) {
        if (type.equals(PROFILE))
            getProfileBanList().remove(profile);
        else
            getIpBanList().remove(profile.getName());
    }

    public boolean isProfileBanned(GameProfile profile) {
        return getProfileBanList().contains(profile);
    }

    public boolean isIpBanned(String banress) {
        return getIpBanList().isBanned(banress);
    }

}
