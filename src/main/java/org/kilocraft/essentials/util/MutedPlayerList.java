package org.kilocraft.essentials.util;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.ServerConfigEntry;
import net.minecraft.server.ServerConfigList;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.mixin.accessor.ServerConfigEntryAccessor;

import java.io.File;
import java.util.Iterator;

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
        String[] strings = new String[this.values().size()];
        int i = 0;

        ServerConfigEntry<GameProfile> configEntry;
        for (Iterator<MutedPlayerEntry> iterator = this.values().iterator(); iterator.hasNext(); strings[i++] = ((ServerConfigEntryAccessor<GameProfile>) configEntry).getKey().getName()) {
            configEntry = iterator.next();
        }

        return strings;
    }

    public String toString(final GameProfile profile) {
        return profile.getId().toString();
    }

}
