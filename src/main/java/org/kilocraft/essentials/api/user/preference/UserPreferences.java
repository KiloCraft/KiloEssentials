package org.kilocraft.essentials.api.user.preference;

import org.kilocraft.essentials.api.NBTSerializable;

public interface UserPreferences extends NBTSerializable {

    /**
     * Sets the value of a Setting for the User
     *
     * @param preference the Setting you want to change the value of
     * @param value the value to set
     * @param <T> Type of the value that Setting accepts
     */
    <T> void set(Preference<T> preference, T value);

    /**
     * Gets the value of a Setting of this User
     *
     * @param preference the Setting you want to get the value of
     * @param <T> the Type of the value that Setting has
     * @return the value of that Setting for this User
     */
    <T> T get(Preference<T> preference);

    /**
     * Resets the value of a Setting for this User
     *
     * @param preference the Setting you want to reset the value of
     * @param <T> the Type of the value that Setting has
     */
    <T> void reset(Preference<T> preference);

}
