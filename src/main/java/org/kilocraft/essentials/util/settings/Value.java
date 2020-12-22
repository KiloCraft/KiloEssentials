package org.kilocraft.essentials.util.settings;

public class Value<K> {

    private K value;

    public Value(K value) {
        this.value = value;
    }

    public K getValue() {
        return value;
    }

    public void setValue(K value) {
        this.value = value;
    }
}
