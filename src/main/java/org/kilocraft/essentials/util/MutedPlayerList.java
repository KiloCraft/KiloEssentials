package org.kilocraft.essentials.util;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.mixin.accessor.StoredUserEntryAccessor;

import java.io.File;
import java.util.Iterator;
import net.minecraft.server.players.StoredUserEntry;
import net.minecraft.server.players.StoredUserList;

public class MutedPlayerList extends StoredUserList<GameProfile, MutedPlayerEntry> {
    public MutedPlayerList(File file) {
        super(file);
    }

    @Override
    protected StoredUserEntry<GameProfile> createEntry(JsonObject jsonObject) {
        return new MutedPlayerEntry(jsonObject);
    }

    @Override
    public boolean contains(@NotNull final GameProfile profile) {
        return super.contains(profile);
    }

    public String[] getUserList() {
        String[] strings = new String[this.getEntries().size()];
        int i = 0;

        StoredUserEntry<GameProfile> configEntry;
        for (Iterator<MutedPlayerEntry> iterator = this.getEntries().iterator(); iterator.hasNext(); strings[i++] = ((StoredUserEntryAccessor<GameProfile>) configEntry).getUser().getName()) {
            configEntry = iterator.next();
        }

        return strings;
    }

    public String toString(final GameProfile profile) {
        return profile.getId().toString();
    }

}
