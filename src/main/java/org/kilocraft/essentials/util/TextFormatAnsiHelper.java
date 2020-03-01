package org.kilocraft.essentials.util;

import org.fusesource.jansi.Ansi;
import org.kilocraft.essentials.api.text.TextFormat;

import java.util.EnumMap;
import java.util.Map;

public class TextFormatAnsiHelper {
    private final Map<TextFormat, String> map = new EnumMap<TextFormat, String>(TextFormat.class);
    private final TextFormat[] formats = TextFormat.values();

    public TextFormatAnsiHelper() {
        map.put(TextFormat.BLACK, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.BLACK).boldOff().toString());
        map.put(TextFormat.DARK_BLUE, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.BLUE).boldOff().toString());
        map.put(TextFormat.DARK_GREEN, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.GREEN).boldOff().toString());
        map.put(TextFormat.DARK_AQUA, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.CYAN).boldOff().toString());
        map.put(TextFormat.DARK_RED, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.RED).boldOff().toString());
        map.put(TextFormat.DARK_PURPLE, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.MAGENTA).boldOff().toString());
        map.put(TextFormat.GOLD, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.YELLOW).boldOff().toString());
        map.put(TextFormat.GRAY, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.WHITE).boldOff().toString());
        map.put(TextFormat.DARK_GRAY, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.BLACK).bold().toString());
        map.put(TextFormat.BLUE, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.BLUE).bold().toString());
        map.put(TextFormat.GREEN, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.GREEN).bold().toString());
        map.put(TextFormat.AQUA, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.CYAN).bold().toString());
        map.put(TextFormat.RED, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.RED).bold().toString());
        map.put(TextFormat.LIGHT_PURPLE, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.MAGENTA).bold().toString());
        map.put(TextFormat.YELLOW, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.YELLOW).bold().toString());
        map.put(TextFormat.WHITE, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.WHITE).bold().toString());
        map.put(TextFormat.OBFUSCATED, Ansi.ansi().a(Ansi.Attribute.BLINK_SLOW).toString());
        map.put(TextFormat.BOLD, Ansi.ansi().a(Ansi.Attribute.UNDERLINE_DOUBLE).toString());
        map.put(TextFormat.STRIKETHROUGH, Ansi.ansi().a(Ansi.Attribute.STRIKETHROUGH_ON).toString());
        map.put(TextFormat.UNDERLINE, Ansi.ansi().a(Ansi.Attribute.UNDERLINE).toString());
        map.put(TextFormat.ITALIC, Ansi.ansi().a(Ansi.Attribute.ITALIC).toString());
        map.put(TextFormat.RESET, Ansi.ansi().a(Ansi.Attribute.RESET).toString());
    }

    @SuppressWarnings("untested")
    public String getFormattedString(String textToFormat) {
        for (TextFormat value : formats) {
            if (map.containsKey(value))
                return textToFormat.replaceAll("(?!)" + value.toString(), map.get(value)) + Ansi.ansi().reset().toString();

            return textToFormat.replaceAll("(?!)" + value.toString(), "" + Ansi.ansi().reset().toString());
        }

        return textToFormat;
    }

}
