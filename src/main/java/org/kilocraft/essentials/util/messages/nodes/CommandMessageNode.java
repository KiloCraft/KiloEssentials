package org.kilocraft.essentials.util.messages.nodes;

public enum CommandMessageNode {
    BAN_NOT_BANNED("command.ban.not_banned"),

    ;

    private final String key;

    CommandMessageNode(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }
}
