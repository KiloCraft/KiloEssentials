package org.kilocraft.essentials.provider;

import java.util.HashMap;
import java.util.Map;

public class SimpleStringSaverProvider {
    private Map<String, String> map;

    public SimpleStringSaverProvider() {
        this.map = new HashMap<>();
    }

    public void save(String key, String value) {
        this.map.put(key, value);
    }

    public void remove(String key) {
        this.map.remove(key);
    }

    public String getValue(String key) {
        return this.map.get(key);
    }

    public void clear() {
        this.map.clear();
    }

    public Map<String, String> getMap() {
        return this.map;
    }
}
