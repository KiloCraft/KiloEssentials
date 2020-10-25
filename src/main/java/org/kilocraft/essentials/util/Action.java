package org.kilocraft.essentials.util;

public interface Action<T> {

    void perform(T t);

    interface Ie<T> {
        T perform();
    }

    interface Returnable<T, R> {
        R perform(T t);
    }

    interface Dynamic2<A, B> {
        void perform(A a, B b);
    }

    interface Dynamic3<A, B, C> {
        void perform(A a, B b, C c);
    }

}
