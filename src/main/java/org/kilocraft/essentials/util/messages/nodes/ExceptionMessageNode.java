package org.kilocraft.essentials.util.messages.nodes;

public enum ExceptionMessageNode {
    EMPTY(""),
    ILLEGAL_STRING_ARGUMENT("illegal_string_argument"),
    INVALID_CHAT_UPSTREAM_ID("invalid_chat_upstream_id"),
    NICKNAME_NOT_ACCEPTABLE("nickname_not_acceptable"),
    TOO_MANY_SELECTIONS("too_many_selections"),
    UNKNOWN_COMMAND_EXCEPTION("unknown_command_exception"),
    ILLEGA_SUDO_LOOP("illegal_sudo_loop"),
    CONTAINS_ENCHANTMENT_NAME("contains_enchantment_name"),
    INCORRECT_IDENTIFIER("incorrect_identifier"),
    INVALID_DYE_COLOR("invalid_dye_color"),
    STRING_TOO_LONG("string_too_long"),
    INVALID("invalid"),
    INTERNAL_ERROR("internal_error"),
    USER_NOT_FOUND("user_not_found"),
    USER_NEVER_JOINED("user_never_joined"),
    USER_CANT_SAVE("user_cant_save"),
    INVALID_ID("invalid_id"),
    NO_VALUE_SET_USER("no_value_set_user"),
    ;


    private String key;

    ExceptionMessageNode(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }

}
