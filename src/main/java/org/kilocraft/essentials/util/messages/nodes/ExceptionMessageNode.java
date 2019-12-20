package org.kilocraft.essentials.util.messages.nodes;

public enum ExceptionMessageNode {
    EMPTY(""),
    ILLEGAL_STRING_ARGUMENT("illegal_string_argument"),
    INVALID_CHAT_UPSTREAM_ID("invalid_chat_upstream_id"),
    NICKNAME_NOT_ACCEPTABLE("nickname_not_acceptable"),
    TOO_MANY_SELECTIONS("too_many_selections"),
    UNKNOWN_COMMAND_EXCEPTION("unknown_command_exception"),
    ILLEGA_SUDO_LOOP("illegal_sudo_loop"),
    ;


    private String key;

    ExceptionMessageNode(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }

}
