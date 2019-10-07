package org.kilocraft.essentials.api.player;

import net.minecraft.entity.SpawnRestriction;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.UUID;

/**
 * Offline representation of a player
 *
 * @author deathsgun
 * @since 1.0.0
 */
public interface IntOfflinePlayer {
    /**
     * Checks if this player is currently online
     *
     * @return true if they are online
     */
    boolean isOnline();

    /**
     * Returns the name of this player
     * <p>
     * Names are no longer unique past a single game session. For persistent storage
     * it is recommended that you use {@link #getUniqueId()} instead.
     *
     * @return Player name or null if we have not seen a name for this player yet
     */
    String getPlayerName();

    /**
     * Returns the UUID of this player
     *
     * @return Player UUID
     */
    UUID getUniqueId();

    /**
     * Checks if this player is banned or not
     *
     * @return true if banned, otherwise false
     */
    boolean isBanned();

    /**
     * Bans or unbans this player
     *
     * @param banned true if banned
     */
    void setBanned(boolean banned);

    /**
     * Checks if this player is whitelisted or not
     *
     * @return true if whitelisted
     */
    boolean isWhitelisted();

    /**
     * Sets if this player is whitelisted or not
     *
     * @param value true if whitelisted
     */
    void setWhitelisted(boolean value);

    /**
     * Gets a {@link ServerPlayerEntity} object that this represents, if there is one
     * <p>
     * If the player is online, this will return that player. Otherwise,
     * it will return null.
     *
     * @return Online player
     */
    ServerPlayerEntity getPlayer();

    /**
     * Gets the first date and time that this player was witnessed on this
     * server.
     * <p>
     * If the player has never played before, this will return 0. Otherwise,
     * it will be the amount of milliseconds since midnight, January 1, 1970
     * UTC.
     *
     * @return Date of first log-in for this player, or 0
     */
    long getFirstPlayed();

    /**
     * Gets the last date and time that this player was witnessed on this
     * server.
     * <p>
     * If the player has never played before, this will return 0. Otherwise,
     * it will be the amount of milliseconds since midnight, January 1, 1970
     * UTC.
     *
     * @return Date of last log-in for this player, or 0
     */
    long getLastPlayed();

    /**
     * Checks if this player has played on this server before.
     *
     * @return True if the player has played before, otherwise false
     */
    boolean hasPlayedBefore();

    /**
     * Gets the Location where the player will spawn at their bed, null if
     * they have not slept in one or their current bed spawn is invalid.
     *
     * @return Bed Spawn Location if bed exists, otherwise null.
     */
    SpawnRestriction.Location getBedSpawnLocation();

    /**
     * Specific properties for the player. They will be
     * saved on every shutdown with Gson.
     *
     * @return a HashMap that can store anything
     */
    HashMap<String, Object> getProperties();

    /**
     * Gets the value of the specified permission, if set.
     * <p>
     * If a permission override is not set on this object, the default value
     * of the permission will be returned.
     *
     * @param name Name of the permission
     * @return Value of the permission
     */
    boolean hasPermission(String name);

    /**
     * Check if the player is a Operator or not
     * @return true | false
     */
    boolean isOp();

    /**
     * Set a player's operator level
     * set it to '0' if you want to remove them
     * @param level op level
     */

    void setOp(int level);

}
