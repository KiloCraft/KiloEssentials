package org.kilocraft.essentials.api.text;

import net.minecraft.text.Style;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ModifierTextPalette extends BaseTextPalette {
    private final Identifier font;

    public ModifierTextPalette(@NotNull Identifier id, @NotNull String code, int intCode, @NotNull String hex)
            throws NumberFormatException {
        this(id, code, intCode, hex, null, null);
    }

    public ModifierTextPalette(@NotNull Identifier id, @NotNull String code, int intCode, @NotNull String hex, @Nullable Identifier font)
            throws NumberFormatException {
        this(id, code, intCode, hex, font, null);
    }

    public ModifierTextPalette(@NotNull Identifier id,
                               @NotNull String code,
                               int intCode,
                               @NotNull String hex,
                               @Nullable FormatterFunction<BaseTextPalette> formatterFunction)
            throws NumberFormatException {
        this(id, code, intCode, hexToRgb(hex), null, formatterFunction);
    }

    public ModifierTextPalette(@NotNull Identifier id,
                               @NotNull String code,
                               int intCode,
                               @NotNull String hex,
                               @Nullable Identifier font,
                               @Nullable FormatterFunction<BaseTextPalette> formatterFunction)
            throws NumberFormatException {
        this(id, code, intCode, hexToRgb(hex), font, formatterFunction);
    }

    public ModifierTextPalette(@NotNull Identifier id, @NotNull String code, int intCode, @NotNull Integer rgb) {
        this(id, code, intCode, rgb, null, null);
    }

    public ModifierTextPalette(@NotNull Identifier id, @NotNull String code, int intCode, @NotNull Integer rgb, @Nullable Identifier font) {
        this(id, code, intCode, rgb, font, null);
    }

    public ModifierTextPalette(@NotNull Identifier id,
                               @NotNull String code,
                               int intCode,
                               @NotNull Integer rgb,
                               @Nullable Identifier font,
                               @Nullable FormatterFunction<BaseTextPalette> formatterFunction) {
        super(id, code, intCode, rgb, formatterFunction);
        this.font = font == null ? Style.DEFAULT_FONT_ID : font;
    }

    public Identifier getFont() {
        return this.font;
    }
}
