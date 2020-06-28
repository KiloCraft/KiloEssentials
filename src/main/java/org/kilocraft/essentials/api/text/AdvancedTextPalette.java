package org.kilocraft.essentials.api.text;

import org.jetbrains.annotations.NotNull;

public class AdvancedTextPalette extends BaseTextPalette {
    private final int rgb;

    public AdvancedTextPalette(@NotNull String name,
                               @NotNull String code,
                               int intCode,
                               boolean isModifier,
                               int rgb) {
        super(name, code, intCode, isModifier);
        this.rgb = rgb;
    }




}
