package org.kilocraft.essentials.api.user;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.feature.FeatureType;
import org.kilocraft.essentials.api.feature.UserProvidedFeature;
import org.kilocraft.essentials.api.user.inventory.UserInventory;
import org.kilocraft.essentials.api.user.settting.Setting;
import org.kilocraft.essentials.api.user.settting.UserSettings;
import org.kilocraft.essentials.api.world.location.Location;
import org.kilocraft.essentials.user.UserHomeHandler;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface User {
    UUID getUuid();

    String getUsername();

    UserSettings getSettings();

    <T> T getSetting(Setting<T> setting);

    boolean isOnline();

    boolean hasNickname();

    String getDisplayName();

    String getFormattedDisplayName();

    Text getRankedDisplayName();

    Text getRankedName();

    String getNameTag();

    Optional<String> getNickname();

    Location getLocation();

    @Nullable
    Location getLastSavedLocation();

    void saveLocation();

    void setNickname(String name);

    void clearNickname();

    void setLastLocation(Location loc);

    boolean hasJoinedBefore();

    @Nullable
    Date getFirstJoin();

    @Nullable
    UUID getLastPrivateMessageSender();

    @Nullable
    String getLastPrivateMessage();

    void setLastMessageSender(UUID uuid);

    void setLastPrivateMessage(String message);

    <F extends UserProvidedFeature> F feature(FeatureType<F> type);

    UserHomeHandler getHomesHandler();

    @Nullable
    String getLastSocketAddress();

    int getTicksPlayed();

    void setTicksPlayed(int ticks);

    /**
     * Saves the data if the user if offline
     */
    void saveData() throws IOException;

    /**
     * Tries to save the user data
     */
    void trySave() throws CommandSyntaxException;

    boolean equals(User anotherUser);

    @Nullable
    UserInventory getInventory();

    boolean ignored(UUID uuid);
}
