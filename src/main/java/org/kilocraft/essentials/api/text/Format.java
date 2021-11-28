package org.kilocraft.essentials.api.text;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;

public enum Format {

    /**
     * Represents black
     */
    BLACK('0', 0x00, ChatFormatting.BLACK, "\\u001b[30m"),
    /**
     * Represents dark blue
     */
    DARK_BLUE('1', 0x1, ChatFormatting.DARK_BLUE, "\\u001b[34m"),
    /**
     * Represents dark green
     */
    DARK_GREEN('2', 0x2, ChatFormatting.DARK_GREEN, "\\u001b[32m"),
    /**
     * Represents dark blue (aqua)
     */
    DARK_AQUA('3', 0x3, ChatFormatting.DARK_AQUA, "\\u001b[36m"),
    /**
     * Represents dark red
     */
    DARK_RED('4', 0x4, ChatFormatting.DARK_RED, "\\u001b[31m"),
    /**
     * Represents dark purple
     */
    DARK_PURPLE('5', 0x5, ChatFormatting.DARK_PURPLE, "\\u001b[35m"),
    /**
     * Represents gold
     */
    GOLD('6', 0x6, ChatFormatting.GOLD, "\\u001b[33m"),
    /**
     * Represents gray
     */
    GRAY('7', 0x7, ChatFormatting.GRAY),
    /**
     * Represents dark gray
     */
    DARK_GRAY('8', 0x8, ChatFormatting.DARK_GRAY),
    /**
     * Represents blue
     */
    BLUE('9', 0x9, ChatFormatting.BLUE, "\\u001b[34;1m"),
    /**
     * Represents green
     */
    GREEN('a', 0xA, ChatFormatting.GREEN, "\\u001b[32;1m"),
    /**
     * Represents aqua
     */
    AQUA('b', 0xB, ChatFormatting.AQUA, "\\u001b[36;1m"),
    /**
     * Represents red
     */
    RED('c', 0xC, ChatFormatting.RED, "\\u001b[31;1m"),
    /**
     * Represents light purple
     */
    LIGHT_PURPLE('d', 0xD, ChatFormatting.LIGHT_PURPLE, "\\u001b[35;1m"),
    /**
     * Represents yellow
     */
    YELLOW('e', 0xE, ChatFormatting.YELLOW, "\\u001b[33;1m"),
    /**
     * Represents white
     */
    WHITE('f', 0xF, ChatFormatting.WHITE, "\\u001b[37;1m"),
    /**
     * Represents magical characters that change around randomly
     */
    OBFUSCATED('k', 0x10, ChatFormatting.OBFUSCATED, true),
    /**
     * Makes the text bold.
     */
    BOLD('l', 0x11, ChatFormatting.BOLD, true, "\\u001b[1m"),
    /**
     * Makes a line appear through the text.
     */
    STRIKETHROUGH('m', 0x12, ChatFormatting.STRIKETHROUGH, true),
    /**
     * Makes the text appear underlined.
     */
    UNDERLINED('n', 0x13, ChatFormatting.UNDERLINE, true, "\\u001b[4m"),
    /**
     * Makes the text italic.
     */
    ITALIC('o', 0x14, ChatFormatting.ITALIC, true),
    /**
     * Resets all previous events colors or formats.
     */
    RESET('r', 0x15, ChatFormatting.RESET);
    public static final char COLOR_CHAR = '\u00A7';
    public static final char ALTERNATIVE_COLOR_CHAR = '&';
    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + COLOR_CHAR + "[0-9A-FK-OR]");

    private final int intCode;
    private final char code;
    private final boolean isFormat;
    private final ChatFormatting formatting;
    private final String toString;
    private final String ansi;
    private static final Map<Character, Format> BY_CHAR = Maps.newHashMap();


    Format(char code, int intCode, ChatFormatting formatting, boolean isFormat, String ansi) {
        this.code = code;
        this.intCode = intCode;
        this.isFormat = isFormat;
        this.formatting = formatting;
        this.toString = new String(new char[]{COLOR_CHAR, code});
        this.ansi = ansi;
    }

    Format(char code, int intCode, ChatFormatting formatting, String ansi) {
        this(code, intCode, formatting, false, ansi);
    }

    Format(char code, int intCode, ChatFormatting formatting) {
        this(code, intCode, formatting, false);
    }

    Format(char code, int intCode, ChatFormatting formatting, boolean isFormat) {
        this(code, intCode, formatting, isFormat, null);
    }

    public static Format getByChar(char code) {
        return BY_CHAR.get(code);
    }

    static {
        for (Format color : values()) {
            BY_CHAR.put(color.code, color);
        }
    }

    @Override
    public String toString() {
        return this.toString;
    }
}
