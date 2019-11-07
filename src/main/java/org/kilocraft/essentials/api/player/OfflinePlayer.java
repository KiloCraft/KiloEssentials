package org.kilocraft.essentials.api.player;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.OperatorEntry;
import net.minecraft.server.WhitelistEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.server.Server;

import java.util.HashMap;
import java.util.UUID;

public class OfflinePlayer implements IntOfflinePlayer { // TODO move to impl package

    private transient Server server;
    private HashMap<String, Object> properties = new HashMap<>();
    private transient GameProfile profile;
    private UUID uniqueId;

    public OfflinePlayer(Server server, GameProfile profile) {
        this.profile = profile;
        this.server = server;
        this.uniqueId = profile.getId();
    }

    @Override
    public boolean isOnline() {
        return getPlayer() != null;
    }

    @Override
    public String getPlayerName() {
        return profile.getName();
    }

    @Override
    public UUID getUniqueId() {
        return uniqueId;
    }

    @Override
    public boolean isBanned() {
        return KiloServer.getServer().getPlayerManager().getUserBanList().contains(profile);
    }

    @Override
    public void setBanned(boolean banned) {
        if (banned == isBanned()) {
            return;
        }
        if (banned) {
            KiloServer.getServer().getPlayerManager().getUserBanList().add(new BannedPlayerEntry(profile));
        } else
            KiloServer.getServer().getPlayerManager().getUserBanList().remove(profile);
    }

    @Override
    public boolean isWhitelisted() {
        return KiloServer.getServer().getPlayerManager().isWhitelisted(profile);
    }

    @Override
    public void setWhitelisted(boolean value) {
        if (value == isWhitelisted()) {
            return;
        }
        if (value)
            KiloServer.getServer().getPlayerManager().getWhitelist().add(new WhitelistEntry(profile));
        else
            KiloServer.getServer().getPlayerManager().getWhitelist().remove(profile);
    }

    @Override
    public ServerPlayerEntity getPlayer() {
        return KiloServer.getServer().getPlayer(uniqueId);
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
    public SpawnRestriction.Location getBedSpawnLocation() {
        return (SpawnRestriction.Location) properties.getOrDefault("bed", null);
    }

    @Override
    public HashMap<String, Object> getProperties() {
        return properties;
    }

    @Override
    public boolean hasPermission(String name) {
        return false;
    }

    @Override
    public boolean isOp() {
        return KiloServer.getServer().getPlayerManager().isOperator(profile);    }

    @Override
    public void setOp(int level) {
        if (isOp() && level >= 1) {
            return;
        }

        if (level >= 1) {
            KiloServer.getServer().getPlayerManager().getOpList().add(new OperatorEntry(profile, 3, false));
        } else {
            KiloServer.getServer().getPlayerManager().getOpList().remove(profile);
        }
    }

    public void setProfile(GameProfile gameProfile) {
        this.profile = gameProfile;
    }

    public void setServer(Server server) {
        this.server = server;
    }
}
