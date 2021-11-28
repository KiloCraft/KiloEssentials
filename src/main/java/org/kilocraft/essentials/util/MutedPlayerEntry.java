package org.kilocraft.essentials.util;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.mixin.accessor.StoredUserEntryAccessor;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class MutedPlayerEntry extends MuteEntry<GameProfile> {
    public MutedPlayerEntry(GameProfile gameProfile) {
        this(gameProfile, null, null, null, null);
    }

    public MutedPlayerEntry(GameProfile gameProfile, @Nullable Date created, @Nullable String source, @Nullable Date expiry, @Nullable String reason) {
        super(gameProfile, created, source, expiry, reason);
    }

    public MutedPlayerEntry(JsonObject jsonObject) {
        super(profileFromJson(jsonObject), jsonObject);
    }

    protected void fromJson(JsonObject jsonObject) {
        if (((StoredUserEntryAccessor<?>) this).getUser() != null) {
            jsonObject.addProperty("uuid", ((GameProfile) ((StoredUserEntryAccessor<?>) this).getUser()).getId() == null ? "" : ((GameProfile) ((StoredUserEntryAccessor<?>) this).getUser()).getId().toString());
            jsonObject.addProperty("name", ((GameProfile) ((StoredUserEntryAccessor<?>) this).getUser()).getName());
            super.serialize(jsonObject);
        }
    }

    public Component toText() {
        GameProfile gameProfile = (GameProfile) ((StoredUserEntryAccessor<?>) this).getUser();
        return new TextComponent(gameProfile.getName() != null ? gameProfile.getName() : Objects.toString(gameProfile.getId(), "(Unknown)"));
    }

    private static GameProfile profileFromJson(JsonObject jsonObject) {
        if (jsonObject.has("uuid") && jsonObject.has("name")) {
            String string = jsonObject.get("uuid").getAsString();

            UUID uUID2;
            try {
                uUID2 = UUID.fromString(string);
            } catch (Throwable var4) {
                return null;
            }

            return new GameProfile(uUID2, jsonObject.get("name").getAsString());
        } else {
            return null;
        }
    }
}
