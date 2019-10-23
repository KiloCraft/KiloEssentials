package org.kilocraft.essentials.craft.provider;

import java.util.Map;

public class SimpleStringSaver {
    private Map<String, String> map;

    public SimpleStringSaver() {
    }

    public void save(String key, String value) {
        map.put(key, value);
    }

    public String getValue(String key) {
        return map.get(key);
    }

    public void clear() {
        map.clear();
    }
}
