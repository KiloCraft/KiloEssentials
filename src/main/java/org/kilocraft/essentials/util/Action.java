package org.kilocraft.essentials.util;

public interface Action<T> {

    void perform(T t);

    interface Returnable<T, R> {
        R perform(T t);
    }
}
