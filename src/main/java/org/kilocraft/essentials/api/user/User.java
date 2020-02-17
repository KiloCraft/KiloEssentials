package org.kilocraft.essentials.api.user;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.feature.FeatureType;
import org.kilocraft.essentials.api.feature.UserProvidedFeature;
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

    boolean isOnline();

    boolean hasNickname();

    String getDisplayName();

    String getFormattedDisplayName();

    Text getRankedDisplayName();

    Text getRankedName();

    String getNameTag();

    List<String> getSubscriptionChannels();

    String getUpstreamChannelId();

    Optional<String> getNickname();

    Location getLocation();

    @Nullable
    Location getLastSavedLocation();

    void saveLocation();

    void setNickname(String name);

    void clearNickname();

    void setLastLocation(Location loc);

    boolean canFly();

    void setFlight(boolean set);

    boolean isSocialSpyOn();

    void setSocialSpyOn(boolean on);

    boolean isCommandSpyOn();

    void setCommandSpyOn(boolean on);

    boolean hasJoinedBefore();

    @Nullable
    Date getFirstJoin();

    void setUpstreamChannelId(String id);

    boolean isInvulnerable();

    void setInvulnerable(boolean set);

    int getRTPsLeft();

    void setRTPsLeft(int amount);

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

    GameMode getGameMode();

    void setGameMode(GameMode mode);

    boolean canSit();

    void setCanSit(boolean set);

    int getTicksPlayed();

    void setTicksPlayed(int ticks);

    /**
     * This should be moved to it's own FeatureType
     * @return
     */
    @Deprecated
    int getDisplayParticleId();

    /**
     * This should be moved to it's own FeatureType
     * @return
     */
    @Deprecated
    void setDisplayParticleId(int i);

    /**
     * Saves the data if the user if offline
     */
    void saveData() throws IOException;

    /**
     * Tries to save the user data
     */
    void trySave() throws CommandSyntaxException;

}
