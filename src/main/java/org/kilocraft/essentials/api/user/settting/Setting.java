package org.kilocraft.essentials.api.user.settting;

public class Setting<T> {
    private String id;
    private T defaultValue;

    public Setting(String id, T defaultValue) {
        this.id = id;
        this.defaultValue = defaultValue;
    }

    public String getId() {
        return this.id;
    }

    public T getDefault() {
        return this.defaultValue;
    }
}
