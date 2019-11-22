package org.kilocraft.essentials.util.messages.nodes;

public enum CommandMessageNode {
    ;

    private String key;

    CommandMessageNode(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }
}
