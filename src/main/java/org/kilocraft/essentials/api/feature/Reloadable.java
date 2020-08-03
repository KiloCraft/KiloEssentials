package org.kilocraft.essentials.api.feature;

public interface Reloadable {
    default void load() {
    }

    default void load(boolean reload) {
    }
}
