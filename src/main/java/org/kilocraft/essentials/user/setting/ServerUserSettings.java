package org.kilocraft.essentials.user.setting;

import net.minecraft.nbt.CompoundTag;
import org.kilocraft.essentials.api.user.settting.Setting;
import org.kilocraft.essentials.api.user.settting.UserSettings;

import java.util.HashMap;
import java.util.Map;

public class ServerUserSettings implements UserSettings {
    private Map<String, Object> map;

    public ServerUserSettings() {
        this.map = new HashMap<>();
    }

    @Override
    public <T> void set(Setting<T> setting, T value) {
        this.map.remove(setting.getId());
        this.map.put(setting.getId(), value);
    }

    @Override
    public <T> T get(Setting<T> setting) {
        return (T) this.map.getOrDefault(setting.getId(), setting.getDefault());
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        this.map.forEach((id, value) -> {
            Setting<?> setting = Settings.getById(id);
            if (setting != null) {
                setting.toTag(tag, value);
            }
        });

        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        for (String key : tag.getKeys()) {
            Setting<?> setting = Settings.getById(key);
            if (setting != null) {
                this.map.put(setting.getId(), setting.fromTag(tag));
            }
        }
    }
}
