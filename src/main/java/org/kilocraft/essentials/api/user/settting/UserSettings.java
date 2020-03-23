package org.kilocraft.essentials.api.user.settting;

import org.kilocraft.essentials.api.NBTSerializable;

public interface UserSettings extends NBTSerializable {

    <T> void set(Setting<T> setting, T value);

    <T> T get(Setting<T> setting);

    <T> T getDefault(Setting<T> setting);

}
