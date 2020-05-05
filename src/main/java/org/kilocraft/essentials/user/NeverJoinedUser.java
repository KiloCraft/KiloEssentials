package org.kilocraft.essentials.user;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.feature.FeatureType;
import org.kilocraft.essentials.api.feature.UserProvidedFeature;
import org.kilocraft.essentials.api.text.MessageReceptionist;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.api.user.settting.Setting;
import org.kilocraft.essentials.api.user.settting.UserSettings;
import org.kilocraft.essentials.api.world.location.Location;

import java.io.IOException;
import java.util.Date;
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
    public UserSettings getSettings() {
        return null;
    }

    @Override
    public <T> T getSetting(Setting<T> setting) {
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
    public boolean hasJoinedBefore() {
        return false;
    }

    @Override
    public @Nullable Date getFirstJoin() {
        return null;
    }

    @Override
    public @Nullable Date getLastOnline() {
        return null;
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
    public int getTicksPlayed() {
        return 0;
    }

    @Override
    public void setTicksPlayed(int ticks) {

    }

    @Override
    public void saveData() throws IOException {

    }

    @Override
    public void trySave() throws CommandSyntaxException {

    }

    @Override
    public boolean equals(User anotherUser) {
        return false;
    }

    @Override
    public boolean ignored(UUID uuid) {
        return false;
    }

    @Override
    public MessageReceptionist getLastDirectMessageReceptionist() {
        return null;
    }

    @Override
    public void setLastDirectMessageReceptionist(MessageReceptionist receptionist) {

    }

    @Override
    public CompoundTag toTag() {
        return null;
    }

    @Override
    public void fromTag(CompoundTag tag) {

    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public UUID getId() {
        return null;
    }
}
