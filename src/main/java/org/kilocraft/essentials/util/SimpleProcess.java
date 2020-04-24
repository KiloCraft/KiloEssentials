package org.kilocraft.essentials.util;

import org.jetbrains.annotations.Nullable;

public class SimpleProcess<T> {
    private String id;
    private T t;

    public SimpleProcess(String id) {
        this.id = id;
    }

    public SimpleProcess(String id, T value) {
        this.id = id;
        this.t = value;
    }

    public String getId() {
        return this.id;
    }

    @Nullable
    public T value() {
        return this.t;
    }
}
