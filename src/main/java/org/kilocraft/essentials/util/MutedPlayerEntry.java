package org.kilocraft.essentials.util;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.mixin.accessor.ServerConfigEntryAccessor;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

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
        if (((ServerConfigEntryAccessor<?>) this).getKey() != null) {
            jsonObject.addProperty("uuid", ((GameProfile) ((ServerConfigEntryAccessor<?>) this).getKey()).getId() == null ? "" : ((GameProfile) ((ServerConfigEntryAccessor<?>) this).getKey()).getId().toString());
            jsonObject.addProperty("name", ((GameProfile) ((ServerConfigEntryAccessor<?>) this).getKey()).getName());
            super.fromJson(jsonObject);
        }
    }

    public Text toText() {
        GameProfile gameProfile = (GameProfile) ((ServerConfigEntryAccessor<?>) this).getKey();
        return new LiteralText(gameProfile.getName() != null ? gameProfile.getName() : Objects.toString(gameProfile.getId(), "(Unknown)"));
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
