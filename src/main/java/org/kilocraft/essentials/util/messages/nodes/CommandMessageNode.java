package org.kilocraft.essentials.util.messages.nodes;

public enum CommandMessageNode {
    EXECUTION_EXCEPTION_HELP("execution_exception_help"),
    BAN_NOT_BANNED("command.ban.not_banned"),

    ;

    private String key;

    CommandMessageNode(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }
}
