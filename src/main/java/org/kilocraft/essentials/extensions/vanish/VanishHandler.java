package org.kilocraft.essentials.extensions.vanish;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.user.ServerUser;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VanishHandler {
    private static List<UUID> vanishedUsers = new ArrayList<>();
    private VanishSettings settings;
    private ServerUser user;

    public VanishHandler(ServerUser user) {
        this.user = user;
        this.settings = new VanishSettings(user);
        vanishedUsers.add(user.getUuid());
    }

    public CompoundTag serialize() {
        return this.settings.serialize();
    }

    public void deserialize(@NotNull CompoundTag compoundTag) {
        this.settings.deserialize(compoundTag);
    }

    public static List<UUID> getVanishedUsers() {
        return vanishedUsers;
    }

    public static boolean isVanished(UUID uuid) {
        return vanishedUsers.contains(uuid);
    }

    public VanishSettings getSettings() {
        if (this.settings == null)
            this.settings = new VanishSettings(this.user);

        return this.settings;
    }

}
