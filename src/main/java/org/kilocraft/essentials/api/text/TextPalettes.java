package org.kilocraft.essentials.api.text;

import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class TextPalettes {
    private static final Map<String, TextFormatPalette> MAP = new HashMap<>();

    static {
        Stream.of(Formatting.values()).filter(Formatting::isColor).forEach((formatting) -> {
            Identifier id = new Identifier(formatting.getName());
            assert formatting.getColorValue() != null;
            MAP.put(
                    id.toString(),
                    new BaseTextPalette(
                            id,
                            Objects.requireNonNull(TextFormat.getCodeByFormatting(formatting)),
                            formatting.getColorIndex(), formatting.getColorValue(),
                            (palette, string) -> new LiteralText(string).formatted(Objects.requireNonNull(TextFormat.getByChar(palette.code())).getFormatting())
                    )
            );
        });


        add(
                new BaseTextPalette(
                        new ResourceLocation("color", "pink"),
                        "p",
                        0x33, "#FFC0CB"
                )
        );
    }

    public static void add(@NotNull final TextFormatPalette palette) {
        MAP.put(palette.getId().toString(), palette);
    }

    public static MutableText stringToComponent(String string) {
        LiteralText component = new LiteralText("");
        //TODO: Regex -> '$\\w'

        return component;
    }


}
