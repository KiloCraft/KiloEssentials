package org.kilocraft.essentials.user;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.feature.FeatureType;
import org.kilocraft.essentials.api.feature.UserProvidedFeature;
import org.kilocraft.essentials.api.world.location.Location;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class NeverJoinedUser implements org.kilocraft.essentials.api.user.NeverJoinedUser {
    @Override
    public UUID getUuid() {
        return null;
    }

    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public boolean isOnline() {
        return false;
    }

    @Override
    public boolean hasNickname() {
        return false;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getFormattedDisplayName() {
        return null;
    }

    @Override
    public Text getRankedDisplayName() {
        return null;
    }

    @Override
    public Text getRankedName() {
        return null;
    }

    @Override
    public String getNameTag() {
        return null;
    }

    @Override
    public List<String> getSubscriptionChannels() {
        return null;
    }

    @Override
    public String getUpstreamChannelId() {
        return null;
    }

    @Override
    public Optional<String> getNickname() {
        return Optional.empty();
    }

    @Override
    public Location getLocation() {
        return null;
    }

    @Override
    public @Nullable Location getLastSavedLocation() {
        return null;
    }

    @Override
    public void saveLocation() {
    }

    @Override
    public void setNickname(String name) {
    }

    @Override
    public void clearNickname() {
    }

    @Override
    public void setLastLocation(Location loc) {

    }

    @Override
    public boolean canFly() {
        return false;
    }

    @Override
    public void setFlight(boolean set) {
    }

    @Override
    public boolean isSocialSpyOn() {
        return false;
    }

    @Override
    public void setSocialSpyOn(boolean on) {

    }

    @Override
    public boolean isCommandSpyOn() {
        return false;
    }

    @Override
    public void setCommandSpyOn(boolean on) {
    }

    @Override
    public boolean hasJoinedBefore() {
        return false;
    }

    @Override
    @Nullable
    public Date getFirstJoin() {
        return null;
    }

    @Override
    public void setUpstreamChannelId(String id) {
    }

    @Override
    public boolean isInvulnerable() {
        return false;
    }

    @Override
    public void setInvulnerable(boolean set) {
    }

    @Override
    public int getRTPsLeft() {
        return 0;
    }

    @Override
    public void setRTPsLeft(int amount) {
    }

    @Override
    @Nullable
    public UUID getLastPrivateMessageSender() {
        return null;
    }

    @Override
    @Nullable
    public String getLastPrivateMessage() {
        return "";
    }

    @Override
    public void setLastMessageSender(UUID uuid) {
    }

    @Override
    public void setLastPrivateMessage(String message) {
    }

    @Override
    public <F extends UserProvidedFeature> F feature(FeatureType<F> type) {
        return null;
    }

    @Override
    public UserHomeHandler getHomesHandler() {
        return null;
    }

    @Override
    public @Nullable String getLastSocketAddress() {
        return null;
    }

    @Override
    public GameMode getGameMode() {
        return null;
    }

    @Override
    public void setGameMode(GameMode mode) {
    }

    @Override
    public boolean canSit() {
        return false;
    }

    @Override
    public void setCanSit(boolean set) {
    }

    @Override
    public int getTicksPlayed() {
        return -1;
    }

    @Override
    public void setTicksPlayed(int minutes) {
    }

    @Override
    public int getDisplayParticleId() {
        return 0;
    }

    @Override
    public void setDisplayParticleId(int i) {
    }

    @Override
    public void saveData() throws IOException {
    }

    @Override
    public void trySave() throws CommandSyntaxException {
    }

}
