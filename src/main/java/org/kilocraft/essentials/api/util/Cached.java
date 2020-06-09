package org.kilocraft.essentials.api.util;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Cached<T> {
    private final String id;
    private final int livesFor;
    private final TimeUnit unit;
    private Date date;
    private T value;

    public Cached(final String id, final T valueToCache) {
        this(id, 30, TimeUnit.MINUTES, valueToCache);
    }

    public Cached(final String id, final int livesFor, final TimeUnit unit, final T valueToCache) {
        this.id = id;
        this.livesFor = livesFor;
        this.unit = unit;
        this.date = new Date(new Date().getTime() + this.unit.toMillis(this.livesFor));
        this.value = valueToCache;
    }

    public void set(T value) {
        this.value = value;
        this.date = new Date(new Date().getTime() + this.unit.toMillis(this.livesFor));
    }

    public boolean isValid() {
        return this.date.getTime() >= new Date().getTime();
    }

    public String getId() {
        return this.id;
    }

    public T get() {
        return this.value;
    }

}
