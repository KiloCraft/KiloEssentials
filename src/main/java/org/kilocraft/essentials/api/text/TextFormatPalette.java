package org.kilocraft.essentials.api.text;

public interface TextFormatPalette {
    char COLOR_CHAR = '\u00A7';

    String name();

    String code();

    boolean isModifier();

    int iniCode();

    default String asString() {
        return COLOR_CHAR + this.code();
    }
}
