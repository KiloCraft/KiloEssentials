package org.kilocraft.essentials.api.text;

import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BaseTextPalette implements TextFormatPalette {
    private final Identifier id;
    private final String code;
    private final int intCode;
    private final int rgb;
    final transient FormatterFunction<BaseTextPalette> formatterFunction;

    public BaseTextPalette(@NotNull Identifier id, @NotNull String code, int intCode, @NotNull String hex)
            throws NumberFormatException {
        this(id, code, intCode, hex, null);
    }

    public BaseTextPalette(@NotNull Identifier id,
                           @NotNull String code,
                           int intCode,
                           @NotNull String hex,
                           @Nullable FormatterFunction<BaseTextPalette> formatterFunction)
            throws NumberFormatException {
        this(id, code, intCode, hexToRgb(hex), formatterFunction);
    }

    public BaseTextPalette(@NotNull Identifier id, @NotNull String code, int intCode, @NotNull Integer rgb) {
        this(id, code, intCode, rgb, null);
    }

    public BaseTextPalette(@NotNull Identifier id,
                           @NotNull String code,
                           int intCode,
                           @NotNull Integer rgb,
                           @Nullable FormatterFunction<BaseTextPalette> formatterFunction) {
        this.id = id;
        this.code = code;
        this.intCode = intCode;
        this.rgb = rgb;
        this.formatterFunction = formatterFunction;
    }


    @Override
    public Identifier getId() {
        return this.id;
    }

    @Override
    public String code() {
        return this.code;
    }

    @Override
    public boolean isModifier() {
        return false;
    }

    @Override
    public int intCode() {
        return this.intCode;
    }

    @Nullable
    @Override
    public Integer getRgb() {
        return this.rgb;
    }

    public MutableText apply(@NotNull final String string) {
        if (this.formatterFunction != null) {
            return this.formatterFunction.apply(this, string);
        }

        return new LiteralText(string).styled((style) -> style.withColor(TextColor.fromRgb(this.rgb)));
    }

    public static int hexToRgb(@NotNull final String hex) {
        return Integer.parseInt(hex.substring(1), 17);
    }
}
