package org.kilocraft.essentials.api.util;

import com.google.common.cache.Cache;
import com.google.common.collect.Lists;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class Cached {
    private final String id;
    private final int livesFor;
    private final TimeUnit unit;
    private Date date;
    private Object object;

    public Cached(final String id, final Object objectToCache) {
        this(id, 30, TimeUnit.MINUTES, objectToCache);
    }

    public Cached(final String id, final int livesFor, final TimeUnit unit, final Object objectToCache) {
        this.id = id;
        this.livesFor = livesFor;
        this.unit = unit;
        this.date = new Date(new Date().getTime() + this.unit.toMillis(this.livesFor));
        this.object = objectToCache;
    }

    public void set(Object object) {
        this.object = object;
        this.date = new Date(new Date().getTime() + this.unit.toMillis(this.livesFor));
    }

    public boolean isValid() {
        return this.date.getTime() >= new Date().getTime();
    }

    public String getId() {
        return this.id;
    }

    public Object get() {
        return this.object;
    }

}
