package org.kilocraft.essentials.api.chat;

import com.google.common.collect.Maps;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.regex.Pattern;

public enum ChatColor {
    /**
     * Represents black
     */
    BLACK('0', 0x00, Formatting.BLACK),
    /**
     * Represents dark blue
     */
    DARK_BLUE('1', 0x1, Formatting.DARK_BLUE),
    /**
     * Represents dark green
     */
    DARK_GREEN('2', 0x2, Formatting.DARK_GREEN),
    /**
     * Represents dark blue (aqua)
     */
    DARK_AQUA('3', 0x3, Formatting.DARK_AQUA),
    /**
     * Represents dark red
     */
    DARK_RED('4', 0x4, Formatting.DARK_RED),
    /**
     * Represents dark purple
     */
    DARK_PURPLE('5', 0x5, Formatting.DARK_PURPLE),
    /**
     * Represents gold
     */
    GOLD('6', 0x6, Formatting.GOLD),
    /**
     * Represents gray
     */
    GRAY('7', 0x7, Formatting.GRAY),
    /**
     * Represents dark gray
     */
    DARK_GRAY('8', 0x8, Formatting.DARK_GRAY),
    /**
     * Represents blue
     */
    BLUE('9', 0x9, Formatting.BLUE),
    /**
     * Represents green
     */
    GREEN('a', 0xA, Formatting.GREEN),
    /**
     * Represents aqua
     */
    AQUA('b', 0xB, Formatting.AQUA),
    /**
     * Represents red
     */
    RED('c', 0xC, Formatting.RED),
    /**
     * Represents light purple
     */
    LIGHT_PURPLE('d', 0xD, Formatting.LIGHT_PURPLE),
    /**
     * Represents yellow
     */
    YELLOW('e', 0xE, Formatting.YELLOW),
    /**
     * Represents white
     */
    WHITE('f', 0xF, Formatting.WHITE),
    /**
     * Represents magical characters that change around randomly
     */
    OBFUSCATED('k', 0x10, Formatting.OBFUSCATED, true),
    /**
     * Makes the text bold.
     */
    BOLD('l', 0x11, Formatting.BOLD ,true),
    /**
     * Makes a line appear through the text.
     */
    STRIKETHROUGH('m', 0x12, Formatting.STRIKETHROUGH, true),
    /**
     * Makes the text appear underlined.
     */
    UNDERLINE('n', 0x13, Formatting.UNDERLINE,true),
    /**
     * Makes the text italic.
     */
    ITALIC('o', 0x14, Formatting.ITALIC,true),
    /**
     * Resets all previous chat colors or formats.
     */
    RESET('r', 0x15, Formatting.RESET);

    public static final char COLOR_CHAR = '\u00A7';
    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + String.valueOf(COLOR_CHAR) + "[0-9A-FK-OR]");

    private final int intCode;
    private final char code;
    private final boolean isFormat;
    private final Formatting formatting;
    private final String toString;
    private static final Map<Integer, ChatColor> BY_ID = Maps.newHashMap();
    private static final Map<Character, ChatColor> BY_CHAR = Maps.newHashMap();

    private ChatColor(char code, int intCode, Formatting formatting) {
        this(code, intCode, formatting, false);
    }

    private ChatColor(char code, int intCode, Formatting formatting, boolean isFormat) {
        this.code = code;
        this.intCode = intCode;
        this.isFormat = isFormat;
        this.formatting = formatting;
        this.toString = new String(new char[] {COLOR_CHAR, code});
    }


    /**
     * Gets the char value associated with this color
     *
     * @return A char value of this color code
     */
    public char getChar() {
        return code;
    }

    public Formatting getFormattingByChar(char code) {
        return formatting;
    }

    @NotNull
    @Override
    public String toString() {
        return toString;
    }

    /**
     * Checks if this code is a format code as opposed to a color code.
     *
     * @return whether this ChatColor is a format code
     */
    public boolean isFormat() {
        return isFormat;
    }

    /**
     * Checks if this code is a color code as opposed to a format code.
     *
     * @return whether this ChatColor is a color code
     */
    public boolean isColor() {
        return !isFormat && this != RESET;
    }

    /**
     * Gets the color represented by the specified color code
     *
     * @param code Code to check
     * @return Associative {@link org.kilocraft.essentials.api} with the given code,
     *     or null if it doesn't exist
     */
    @Nullable
    public static ChatColor getByChar(char code) {
        return BY_CHAR.get(code);
    }

    /**
     * Gets the color represented by the specified color code
     *
     * @param code Code to check
     * @return Associative {@link org.kilocraft.essentials.api} with the given code,
     *     or null if it doesn't exist
     */
    @Nullable
    public static ChatColor getByChar(@NotNull String code) {
        Validate.notNull(code, "Code cannot be null");
        Validate.isTrue(code.length() > 0, "Code must have at least one char");

        return BY_CHAR.get(code.charAt(0));
    }

    /**
     * Strips the given message of all color codes
     *
     * @param input String to strip of color
     * @return A copy of the input string, without any coloring
     */
    @Contract("!null -> !null; null -> null")
    @Nullable
    public static String stripColor(@Nullable final String input) {
        if (input == null) {
            return null;
        }

        return STRIP_COLOR_PATTERN.matcher(input).replaceAll("");
    }

    /**
     * Translates a string using an alternate color code character into a
     * string that uses the internal ChatColor.COLOR_CODE color code
     * character. The alternate color code character will only be replaced if
     * it is immediately followed by 0-9, A-F, a-f, K-O, k-o, R or r.
     *
     * @param altColorChar The alternate color code character to replace. Ex: {@literal &}
     * @param textToTranslate Text containing the alternate color code character.
     * @return Text containing the ChatColor.COLOR_CODE color code character.
     */


    @NotNull
    public static String translateAlternateColorCodes(char altColorChar, @NotNull String textToTranslate) {
        Validate.notNull(textToTranslate, "Cannot translate null text");
        char[] b = textToTranslate.toCharArray();

        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == altColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i+1]) > -1) {
                b[i] = ChatColor.COLOR_CHAR;
                b[i+1] = Character.toLowerCase(b[i+1]);
            }
        }
        return new String(b);
    }

    public static LiteralText translateToLiteralText(char altColorChar, @NotNull String textToTranslate) {
        Validate.notNull(textToTranslate, "Cannot translate null text");
        LiteralText literalText = new LiteralText("");
        char[] b = textToTranslate.toCharArray();

        for (int i = 0; i < b.length -1; i++) {
            if (b[i] == altColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i+1]) > -1) {

            }

        }

        return literalText;
    }


    public static String removeAlternateColorCodes(char altColorChar, @NotNull String textToTranslate) {
        Validate.notNull(textToTranslate, "Cannot remove color codes from null text");

        return textToTranslate.replaceAll(String.valueOf(altColorChar), "");
    }

    /**
     * Gets the ChatColors used at the end of the given input string.
     *
     * @param input Input string to retrieve the colors from.
     * @return Any remaining ChatColors to pass onto the next line.
     */
    @NotNull
    public static String getLastColors(@NotNull String input) {
        Validate.notNull(input, "Cannot get last colors from null text");

        String result = "";
        int length = input.length();

        // Search backwards from the end as it is faster
        for (int index = length - 1; index > -1; index--) {
            char section = input.charAt(index);
            if (section == COLOR_CHAR && index < length - 1) {
                char c = input.charAt(index + 1);
                ChatColor color = getByChar(c);

                if (color != null) {
                    result = color.toString() + result;

                    // Once we find a color or reset we can stop searching
                    if (color.isColor() || color.equals(RESET)) {
                        break;
                    }
                }
            }
        }

        return result;
    }

    static {
        for (ChatColor color : values()) {
            BY_ID.put(color.intCode, color);
            BY_CHAR.put(color.code, color);
        }
    }
}
