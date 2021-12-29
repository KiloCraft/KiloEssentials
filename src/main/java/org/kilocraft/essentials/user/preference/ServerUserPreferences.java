package org.kilocraft.essentials.user.preference;

import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.user.preference.Preference;
import org.kilocraft.essentials.api.user.preference.UserPreferences;

import java.util.Map;
import net.minecraft.nbt.CompoundTag;

public class ServerUserPreferences implements UserPreferences {
    private final Map<String, Object> SETTINGS;

    public ServerUserPreferences() {
        this.SETTINGS = Maps.newHashMap();
    }

    @Override
    public <T> void set(Preference<T> preference, T value) {
        this.SETTINGS.put(preference.getId(), value);
    }

    @Override
    public <T> T get(Preference<T> preference) {
        return (T) this.SETTINGS.getOrDefault(preference.getId(), preference.getDefault());
    }

    @Override
    public <T> void reset(Preference<T> preference) {
        this.SETTINGS.remove(preference.getId());
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        for (Map.Entry<String, Object> entry : this.SETTINGS.entrySet()) {
            Preference<?> preference = Preferences.getById(entry.getKey());
            if (preference != null) {
                try {
                    preference.toTag(tag, entry.getValue());
                } catch (IllegalArgumentException e) {
                    KiloEssentials.getLogger().fatal("Exception while serializing a User Setting: Can not save the Value", e);
                }
            }
        }

        return tag;
    }

    @Override
    public void fromTag(@NotNull final CompoundTag tag) {
        for (String key : tag.getAllKeys()) {
            Preference<?> preference = Preferences.getById(key);
            if (preference != null) {
                try {
                    this.SETTINGS.put(preference.getId(), preference.fromTag(tag));
                } catch (IllegalArgumentException e) {
                    KiloEssentials.getLogger().fatal("Exception while de-serializing a User Setting: Using Default Value", e);
                }
            }
        }
    }
}
