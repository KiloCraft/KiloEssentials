package org.kilocraft.essentials.util;

import org.kilocraft.essentials.api.util.Cached;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class CacheManager {
    private static final Map<String, Cached<?>> map = new HashMap<>();

    public static void cache(Cached<?>... cached) {
        Objects.requireNonNull(cached, "Cache entry must not be null!");

        for (Cached<?> c : cached) {
            map.put(c.getId(), c);
        }
    }

    public static Cached<?> get(String id) {
        return map.get(id);
    }

    public static boolean isCached(String id) {
        return map.containsKey(id);
    }

    public static boolean shouldUse(String id) {
        return isCached(id) && get(id).isValid();
    }

    public static void getAndRun(String id, Consumer<Cached<?>> consumer) {
        Cached<?> cached = get(id);
        consumer.accept(cached);
    }

}
