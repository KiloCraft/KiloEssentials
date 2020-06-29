package org.kilocraft.essentials.api.text;

import net.minecraft.text.MutableText;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public interface TextFormatPalette {
    char COLOR_CHAR = '\u00A7';

    /**
     * Gets the {@link Identifier} for this Palette
     * @return Identifier of this palette
     */
    Identifier getId();

    /**
     * Gets the String Code of this Palette
     * @return Code of this Palette
     */
    String code();

    /**
     * If true, this Palette will modify more than just the color
     * @return can modify the text
     */
    boolean isModifier();

    /**
     * Gets the Integer Code of this Palette
     * @return int code of this Palette
     */
    int intCode();

    /**
     * Gets the RGB value of this Palette
     * @return RGB value of this Palette as Integer
     */
    @Nullable
    Integer getRgb();

    default String asString() {
        return COLOR_CHAR + this.code();
    }

    interface FormatterFunction<P extends TextFormatPalette> {
        MutableText apply(P pattern, String string);
    }
}
