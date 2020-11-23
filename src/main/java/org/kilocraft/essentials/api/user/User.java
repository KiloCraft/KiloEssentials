package org.kilocraft.essentials.api.user;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.NBTSerializable;
import org.kilocraft.essentials.api.user.preference.Preference;
import org.kilocraft.essentials.api.user.preference.UserPreferences;
import org.kilocraft.essentials.api.util.EntityIdentifiable;
import org.kilocraft.essentials.api.world.location.Location;
import org.kilocraft.essentials.user.UserHomeHandler;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * User, better way for handling Data related to Players
 *
 * @see org.kilocraft.essentials.user.ServerUser
 * @see OnlineUser
 * @see CommandSourceUser
 * @see NeverJoinedUser
 * @see UserHomeHandler
 * @see org.kilocraft.essentials.user.UserHandler
 * @see net.minecraft.entity.player.PlayerEntity
 * @see net.minecraft.server.network.ServerPlayerEntity
 * @since 1.5
 * @author CODY_AI (OnBlock)
 */
public interface User extends EntityIdentifiable, NBTSerializable {
    /**
     * Gets the Unique Unified Identifier of this User
     * @return UUID of this user
     */
    UUID getUuid();

    /**
     * Gets the Username (Name of the Mojang Account) of this User
     * @return Username of this user
     */
    String getUsername();

    /**
     * Gets the Settings for this user
     * @return UserPreference
     */
    UserPreferences getPreferences();

    /**
     * Get a Setting for this user
     *
     * @param preference The Preference to Get
     * @param <T> Type of the Preference (value)
     * @return the value of that Preference (T)
     */
    <T> T getPreference(Preference<T> preference);

    /**
     * Checks if the user is online or not
     * @return online
     */
    boolean isOnline();

    /**
     * Checks if the user has a Nickname or not
     * @return has nickname
     */
    boolean hasNickname();

    /**
     * Gets the Display name of this user as a String
     * @return Display name as String
     */
    String getDisplayName();

    /**
     * Gets the Display name of this user a Formatted String
     * @return Display name as Formatted String
     */
    String getFormattedDisplayName();

    /**
     * Gets the modified Display name as a {@link Text}
     * @return Team modified Display name
     */
    Text getRankedDisplayName();

    /**
     * Gets the modified Display name as a {@link String}
     * @return Team modified Display name
     */
    String getRankedDisplayNameAsString();

    /**
     * Gets the modified name as a {@link Text}
     * @return Team modified name
     */
    Text getRankedName();

    /**
     * Gets the name tag of this user (can be configured through the config file)
     * @return name tag
     */
    String getNameTag();

    /**
     * Gets the nickname of this user
     * @return Optional of nullable "Nickname"
     */
    Optional<String> getNickname();

    /**
     * Gets the Location of this user {@link Location}
     *
     * @see Location
     * @see org.kilocraft.essentials.api.world.location.Vec3dLocation
     * @see org.kilocraft.essentials.api.world.location.Vec3iLocation
     * @return current location of this user
     */
    Location getLocation();

    /**
     * Gets the last saved location of this user {@link Location}
     * Can be NULL
     *
     * @see Location
     * @see org.kilocraft.essentials.api.world.location.Vec3dLocation
     * @see org.kilocraft.essentials.api.world.location.Vec3iLocation
     * @return last saved Location of this user
     */
    @Nullable
    Location getLastSavedLocation();

    /**
     * Saves the location of this user
     */
    void saveLocation();

    /**
     * Sets the nickname of this user
     * @param name name to set
     */
    void setNickname(String name);

    /**
     * Clears the nickname of this user
     */
    void clearNickname();

    /**
     * Sets the last location of this user
     * @param loc {@link Location} to set
     */
    void setLastLocation(Location loc);

    /**
     * Checks if the user has joined before or not
     * @return has joined before
     */
    boolean hasJoinedBefore();

    /**
     * Gets the first join {@link Date} of this user
     * @return the first join Date
     */
    @Nullable
    Date getFirstJoin();

    /**
     * Gets the last online {@link Date} of this user
     * @return the last online Date
     */
    @Nullable
    Date getLastOnline();

    /**
     * Gets the HomeHandler of this user
     * Can be NULL if the Feature isn't enabled
     * @return Home handler of this user
     */
    @Nullable
    UserHomeHandler getHomesHandler();

    /**
     * The last saved Socket Address of this user
     * @return last socket address as String
     */
    @Nullable
    String getLastSocketAddress();

    /**
     * The last saved Ip Address of this user
     * @return last Ip Address as String
     */
    @Nullable
    String getLastIp();

    /**
     * Gets the amount of playtime as ticks
     * @return ticks played
     */
    int getTicksPlayed();

    /**
     * Sets the amount of playtime as ticks
     * @param ticks amount of ticks to set
     */
    void setTicksPlayed(int ticks);

    /**
     * Saves the data if the user if offline
     */
    void saveData() throws IOException;

    /**
     * Tries to save the user data
     */
    void trySave() throws CommandSyntaxException;

    /**
     * Checks if this user is Equals to another User
     * @param anotherUser user to check for
     * @return is This equals to another user
     */
    boolean equals(User anotherUser);

    /**
     * Checks if this user is ignoring another user
     * @param uuid the {@link UUID} of the other user to check
     * @return is This user ignoring the other user
     */
    boolean ignored(UUID uuid);

    EntityIdentifiable getLastMessageReceptionist();

    void setLastMessageReceptionist(EntityIdentifiable entity);

}
