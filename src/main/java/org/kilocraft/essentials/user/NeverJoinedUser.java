package org.kilocraft.essentials.user;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.feature.FeatureType;
import org.kilocraft.essentials.api.feature.UserProvidedFeature;

import java.util.Date;
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
    @Nullable
    public String getNickname() {
        return "";
    }

    @Override
    public void setNickname(String name) {
    }

    @Override
    public void clearNickname() {
    }

    @Override
    @Nullable
    public Identifier getBackDimId() {
        return null;
    }

    @Override
    @Nullable
    public Vec3d getBackPos() {
        return null;
    }

    @Override
    public void setBackPos(Vec3d position) {
    }

    @Override
    public void setBackDim(Identifier dim) {
    }

    @Override
    public Identifier getPosDim() {
        return null;
    }

    @Override
    @Nullable
    public Vec3d getPos() {
        return null;
    }

    @Override
    public boolean canFly() {
        return false;
    }

    @Override
    public void setFlight(boolean set) {

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
    public <F extends UserProvidedFeature> F feature(FeatureType<F> type) {
        return null;
    }

    @Override
    public UserHomeHandler getHomesHandler() {
        return null;
    }

    @Override
    public int getDisplayParticleId() {
        return 0;
    }

    @Override
    public void setDisplayParticleId(int i) {
    }
}
