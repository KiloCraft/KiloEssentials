package org.kilocraft.essentials.util.messages.nodes;

public enum ExceptionMessageNode {
    EMPTY(""),
    TOO_MANY_SELECTIONS("too_many_selections"),
    ILLEGAL_STRING_ARGUMENT("illegal_string_argument"),
    UNKNOWN_COMMAND_EXCEPTION("unknown_command_exception"),
    NICKNAME_NOT_ACCEPTABLE("nickname_not_acceptable")
    ;


    private String key;

    ExceptionMessageNode(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }

}
