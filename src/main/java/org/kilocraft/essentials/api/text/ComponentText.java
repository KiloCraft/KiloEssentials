package org.kilocraft.essentials.api.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.ModConstants;

import java.util.Locale;

public class ComponentText {

    /**
     * Translates a string with legacy style color codes into a Kyori Adventure style.
     *
     * @param text Text containing the alternate color code character.
     * @return String containing the new formatting code style.
     */
    public static String updateLegacyStyle(@NotNull final String text) {
        Validate.notNull(text, "Cannot translate null text");
        if (!text.contains("&")) return text;
        String string = text;
        final char[] b = text.toCharArray();
        for (int i = 0; i < b.length; i++) {
            if (b[i] == '&' && i != b.length - 1 && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i + 1]) > -1) {
                final @Nullable Format format = Format.getByChar(b[i + 1]);
                string = string.replace(
                        String.valueOf('&') + b[i + 1],
                        "<" + (format == null ? "reset" : format.name().toLowerCase(Locale.ENGLISH)) + ">"
                );
            }
        }
        return string;
    }

    public static String clearFormatting(@NotNull String textToClear) {
        return stripRainbow(stripGradient(stripEvent(stripFormatting(stripColor(textToClear)))));
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

    public static Component of(@NotNull final String string) {
        Validate.notNull(string, "String must not be null!");
        return MiniMessage.miniMessage().parse(updateLegacyStyle(string));
    }

    public static Component removeEvents(@NotNull final Component component) {
        component.style(s -> s.clickEvent(null).hoverEvent(null));
        for (Component c : component.children()) {
            removeEvents(c);
        }
        return component;
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

    public static String translateOld(String input) {
        return input.replaceAll("&([a-fk-or0-9])", "§$1");
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
        NamedTextColor color;
        if (tps > 15.0D) {
            color = NamedTextColor.GREEN;
        } else if (tps > 10) {
            color = NamedTextColor.YELLOW;
        } else {
            color = NamedTextColor.RED;
        }

        return "<" + color + ">" + ModConstants.DECIMAL_FORMAT.format(tps);
    }

    public static String formatMspt(final double tps) {
        NamedTextColor color;
        if (tps > 50.0D) {
            color = NamedTextColor.RED;
        } else if (tps > 45.0D) {
            color = NamedTextColor.YELLOW;
        } else {
            color = NamedTextColor.GREEN;
        }

        return "<" + color + ">" + ModConstants.DECIMAL_FORMAT.format(tps);
    }

    public static String stripColor(String s) {
        return s.replaceAll("<\\/?((color:#\\w+)|(#\\w+)|((color:)?(black|dark_blue|dark_green|dark_aqua|dark_red|dark_purple|gold|gray|dark_gray|blue|green|aqua|red|light_purple|yellow|white)))>", "").replaceAll("&[a-f0-9]", "");
    }

    public static String stripFormatting(String s) {
        return s.replaceAll("<\\/?(bold|italic|underlined|obfuscated|strikethrough|reset)>", "").replaceAll("&[k-o]", "");
    }

    public static String stripEvent(String s) {
        return s.replaceAll("<\\/?(((click|insertion)(:[^<>]+)*)|hover:\\w*:('|\")[<>\\w]+('|\"))>", "");
    }

    public static String stripGradient(String s) {
        return s.replaceAll("<\\/?gradient(:#?\\w+)*>", "");
    }

    public static String stripRainbow(String s) {
        return s.replaceAll("<\\/?rainbow>", "");
    }

}
