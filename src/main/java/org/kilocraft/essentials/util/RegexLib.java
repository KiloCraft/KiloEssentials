package org.kilocraft.essentials.util;

public enum RegexLib {
    DIGITS("\\d+"),
    START_WITH_DIGITS("^\\d+"),
    ALPHABEITC("[a-zA-Z]+")
    ;


    private String regex;

    RegexLib(String regex) {
        this.regex = regex;
    }

    public String get() {
        return this.regex;
    }

}
