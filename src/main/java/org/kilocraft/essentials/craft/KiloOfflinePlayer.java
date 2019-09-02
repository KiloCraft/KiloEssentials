package org.kilocraft.essentials.craft;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.OperatorEntry;
import net.minecraft.server.WhitelistEntry;
import org.kilocraft.essentials.api.entity.OfflinePlayer;

import java.util.HashMap;
import java.util.UUID;

public class KiloOfflinePlayer implements OfflinePlayer {

    private transient MinecraftServer server;
    private HashMap<String, Object> properties = new HashMap<>();
    private transient GameProfile profile;
    private UUID uniqueId;

    public KiloOfflinePlayer(MinecraftServer server, GameProfile profile) {
        this.profile = profile;
        this.server = server;
        this.uniqueId = profile.getId();
    }


    @Override
    public boolean isOperator() {
        return server.getPlayerManager().getOpList().isOp(profile);
    }

    @Override
    public boolean isOnline() {
        return getPlayer() != null;
    }

    @Override
    public String getName() {
        return profile.getName();
    }

    @Override
    public String getDisplayName() {
        return getPlayer().getDisplayName().asString();
    }

    @Override
    public UUID getUniqeId() {
        return uniqueId;
    }

    @Override
    public boolean isBanned() {
        return server.getPlayerManager().getUserBanList().contains(profile);
    }

    @Override
    public void setBanned(boolean banned) {
        if (banned == isBanned()) return;
        if (banned) {
            server.getPlayerManager().getUserBanList().add(new BannedPlayerEntry(profile));
        } else server.getPlayerManager().getUserBanList().remove(profile);
    }

    @Override
    public boolean isWhitelisted() {
        return server.getPlayerManager().isWhitelisted(profile);
    }

    @Override
    public void setWhitelisted(boolean whitelisted) {
        if (whitelisted == isWhitelisted()) return;
        if (whitelisted)
            server.getPlayerManager().getWhitelist().add(new WhitelistEntry(profile));
        else
            server.getPlayerManager().getWhitelist().remove(profile);
    }

    @Override
    public PlayerEntity getPlayer() {
        return server.getPlayerManager().getPlayer(getUniqeId());
    }

    @Override
    public long getFirstPlayed() {
        return (long) properties.getOrDefault("firstPlayed", 0);
    }

    @Override
    public long getLastPlayed() {
        return (long) properties.getOrDefault("lastPlayed", 0);
    }

    @Override
    public boolean hasPlayedBefore() {
        return (boolean) properties.getOrDefault("playedBefore", false);
    }

    @Override
    public SpawnRestriction.Location getSpawnLocation() {
        return (SpawnRestriction.Location) properties.getOrDefault("bed", null);
    }

    @Override
    public HashMap<String, Object> getProperties() {
        return properties;
    }

    @Override
    public boolean hasPermission(String permissionId) {
        if (isOperator()) return true;
        else return false;
    }

    public void setProperties(HashMap<String, Object> properties) {
        this.properties = properties;
    }

    public void setServer(MinecraftServer minecraftServer) {
        this.server = minecraftServer;
    }

    @Override
    public void setOperator(boolean operator) {
        if (operator == isOperator()) return;
        if (operator) {
            server.getPlayerManager().getOpList().add(new OperatorEntry(profile, 3, false));
        } else {
            server.getPlayerManager().getOpList().remove(profile);
        }
    }

    public void setProfile(GameProfile profile) {
        this.profile = profile;
    }
}
