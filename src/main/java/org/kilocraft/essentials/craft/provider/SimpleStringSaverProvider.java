package org.kilocraft.essentials.craft.provider;

import java.util.HashMap;
import java.util.Map;

public class SimpleStringSaverProvider {
    private Map<String, String> map = new HashMap<>();

    public SimpleStringSaverProvider() {
    }

    public void save(String key, String value) {
        map.put(key, value);
    }

    public void remove(String key) {
        map.remove(key);
    }

    public String getValue(String key) {
        return map.get(key);
    }

    public void clear() {
        map.clear();
    }
}
