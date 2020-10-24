package org.kilocraft.essentials.api.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class ComponentText {
    public static Text empty() {
        return new LiteralText("");
    }

    /**
     * Translates a string with legacy style color codes into a Kyori Adventure style.
     *
     * @param text Text containing the alternate color code character.
     * @return String containing the new formatting code style.
     */
    public static String updateLegacyStyle(@NotNull final String text) {
        Validate.notNull(text, "Cannot translate null text");
        String string = text;
        final char[] b = text.toCharArray();
        for (int i = 0; i < b.length; i++) {
            if (b[i] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i + 1]) > -1) {
                final @Nullable TextFormat format = TextFormat.getByChar(b[i + 1]);
                string = string.replace(
                        String.valueOf('&') + b[i + 1],
                        "<" + (format == null ? "reset" : format.name().toLowerCase(Locale.ENGLISH)) + ">"
                );
            }
        }
        return string;
    }

    public static String clearColorCodes(@NotNull String textToClear) {
        return textToClear.replaceAll("<.+:.+>", "");
    }

    public static Text toText(@NotNull final Component component) {
        Validate.notNull(component, "Component must not be null!");
        return Text.Serializer.fromJson(GsonComponentSerializer.gson().serialize(component));
    }

    public static MutableText toText(@NotNull final String raw) {
        Validate.notNull(raw, "Input must not be null!");
        return Text.Serializer.fromJson(GsonComponentSerializer.gson().serialize(of(raw)));
    }

    public static Component toComponent(@NotNull final Text text) {
        Validate.notNull(text, "Text must not be null!");
        return GsonComponentSerializer.gson().deserialize(Text.Serializer.toJson(text));
    }

    public static Component of(@NotNull final String raw) {
        return of(raw, true);
    }

    public static Component of(@NotNull final String raw, final boolean markdown) {
        Validate.notNull(raw, "String must not be null!");
        final String string = raw.contains("&") ? updateLegacyStyle(raw) : raw;
        if (markdown) {
            return MiniMessage.markdown().parse(string);
        }
        return MiniMessage.get().parse(string);
    }

    public static Component removeEvents(@NotNull final Component component) {
        return component.clickEvent(null).hoverEvent(null);
    }

    public static Component removeStyle(@NotNull final Component component) {
        return component.style(Style.empty());
    }

    public static String formatPercentage(final double percentage) {
        String color;
        if (percentage >= 80.0D) {
            color = NamedTextColor.GREEN.toString();
        } else if (percentage < 40.0D) {
            color = NamedTextColor.YELLOW.toString();
        } else {
            color = NamedTextColor.RED.toString();
        }

        return "<" + color + ">" + percentage;
    }

    public static String formatPing(final double milliseconds) {
        String color;
        if (milliseconds < 200.0D) {
            color = NamedTextColor.GREEN.toString();
        } else if (milliseconds < 400.0D) {
            color = NamedTextColor.YELLOW.toString();
        } else {
            color = NamedTextColor.RED.toString();
        }

        return "<" + color + ">" + milliseconds;
    }

    public static String formatTps(final double tps) {
        String color;
        if (tps > 15.0D) {
            color = NamedTextColor.GREEN.toString();
        } else if (tps > 10) {
            color = NamedTextColor.YELLOW.toString();
        } else {
            color = NamedTextColor.RED.toString();
        }

        return "<" + color + ">" + tps;
    }
}
