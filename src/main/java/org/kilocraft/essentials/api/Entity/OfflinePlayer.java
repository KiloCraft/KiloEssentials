package org.kilocraft.essentials.api.Entity;

import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;
import java.util.UUID;

public interface OfflinePlayer {

    boolean isOperator();

    boolean isOnline();

    String getName();

    String getDisplayName();

    UUID getUniqeId();

    boolean isBanned();

    void setBanned(boolean banned);

    boolean isWhitelisted();

    void setWhitelisted(boolean whitelisted);

    PlayerEntity getPlayer();

    long getFirstPlayed();

    long getLastPlayed();

    boolean hasPlayedBefore();

    SpawnRestriction.Location getSpawnLocation();

    HashMap<String, Object> getProperties();

    boolean hasPermission(String permissionId);

    void setOperator(boolean operator);
}
