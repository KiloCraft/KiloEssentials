package org.kilocraft.essentials.util;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.ServerConfigEntry;
import net.minecraft.server.ServerConfigList;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class MutedPlayerList extends ServerConfigList<GameProfile, MutedPlayerEntry> {
    public MutedPlayerList(File file) {
        super(file);
    }

    @Override
    protected ServerConfigEntry<GameProfile> fromJson(JsonObject jsonObject) {
        return new MutedPlayerEntry(jsonObject);
    }

    @Override
    public boolean contains(@NotNull final GameProfile profile) {
        return super.contains(profile);
    }

    public String[] getNames() {
        return null;
    }

    public String toString(final GameProfile profile) {
        return profile.getId().toString();
    }

}
