package org.kilocraft.essentials.api.user.settting;

import org.kilocraft.essentials.api.NBTSerializable;

public interface UserSettings extends NBTSerializable {

    /**
     * Sets the value of a Setting for the User
     *
     * @param setting the Setting you want to change the value of
     * @param value the value to set
     * @param <T> Type of the value that Setting accepts
     */
    <T> void set(Setting<T> setting, T value);

    /**
     * Gets the value of a Setting of this User
     *
     * @param setting the Setting you want to get the value of
     * @param <T> the Type of the value that Setting has
     * @return the value of that Setting for this User
     */
    <T> T get(Setting<T> setting);

    /**
     * Resets the value of a Setting for this User
     *
     * @param setting the Setting you want to reset the value of
     * @param <T> the Type of the value that Setting has
     */
    <T> void reset(Setting<T> setting);

}
