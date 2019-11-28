package org.kilocraft.essentials.util;

public enum RegexLib {
    DIGITS_ONLY("^\\d+"),
    ALL_EXCEPT_DIGITS("[a-zA-Z]+")
    ;


    private String regex;

    private RegexLib(String regex) {
        this.regex = regex;
    }

    public String get() {
        return this.regex;
    }

}
