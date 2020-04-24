package org.kilocraft.essentials.util;

public enum RegexLib {
    DIGITS("\\d+"),
    START_WITH_DIGITS("^\\d+"),
    ALPHABETIC("[a-zA-Z]+"),
    URL("\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");


    private String regex;

    RegexLib(String regex) {
        this.regex = regex;
    }

    public String get() {
        return this.regex;
    }

}
