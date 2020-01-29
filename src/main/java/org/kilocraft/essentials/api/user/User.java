package org.kilocraft.essentials.api.user;

import net.minecraft.text.Text;
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

    boolean canSit();

    void setCanSit(boolean set);

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

}
