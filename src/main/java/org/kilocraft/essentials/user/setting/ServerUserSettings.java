package org.kilocraft.essentials.user.setting;

import net.minecraft.nbt.CompoundTag;
import org.kilocraft.essentials.api.user.settting.Setting;
import org.kilocraft.essentials.api.user.settting.UserSettings;
import org.kilocraft.essentials.user.ServerUser;

import java.util.HashMap;
import java.util.Map;

public class ServerUserSettings implements UserSettings {
    private Map<Setting<?>, Object> map;

    public ServerUserSettings(ServerUser user) {
        this.map = new HashMap<>();
    }

    @Override
    public <T> void set(Setting<T> setting, T value) {
        this.map.remove(setting);
        this.map.put(setting, value);
    }

    @Override
    public <T> T get(Setting<T> setting) {
        return (T) this.map.getOrDefault(setting, setting.getDefault());
    }

    @Override
    public <T> T getDefault(Setting<T> setting) {
        return setting.getDefault();
    }

    @Override
    public CompoundTag toTag() {
        return null;
    }

    @Override
    public void fromTag(CompoundTag tag) {

    }
}
