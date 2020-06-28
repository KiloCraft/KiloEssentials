package org.kilocraft.essentials.api.text;

import org.jetbrains.annotations.NotNull;

public class BaseTextPalette implements TextFormatPalette {
    private final String name;
    private final String code;
    private final int intCode;
    private final boolean modifier;

    public BaseTextPalette(@NotNull final String name,
                           @NotNull final String code,
                           final int intCode,
                           final boolean isModifier) {
        this.name = name;
        this.code = code;
        this.intCode = intCode;
        this.modifier = isModifier;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public String code() {
        return this.code;
    }

    @Override
    public boolean isModifier() {
        return this.modifier;
    }

    @Override
    public int iniCode() {
        return this.intCode;
    }
}
