package org.kilocraft.essentials.api.text;

import com.google.common.collect.Maps;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.commands.CommandUtils;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Deprecated
public enum TextFormat {
    /**
     * Represents black
     */
    BLACK('0', 0x00, Formatting.BLACK, "\\u001b[30m"),
    /**
     * Represents dark blue
     */
    DARK_BLUE('1', 0x1, Formatting.DARK_BLUE, "\\u001b[34m"),
    /**
     * Represents dark green
     */
    DARK_GREEN('2', 0x2, Formatting.DARK_GREEN, "\\u001b[32m"),
    /**
     * Represents dark blue (aqua)
     */
    DARK_AQUA('3', 0x3, Formatting.DARK_AQUA, "\\u001b[36m"),
    /**
     * Represents dark red
     */
    DARK_RED('4', 0x4, Formatting.DARK_RED, "\\u001b[31m"),
    /**
     * Represents dark purple
     */
    DARK_PURPLE('5', 0x5, Formatting.DARK_PURPLE, "\\u001b[35m"),
    /**
     * Represents gold
     */
    GOLD('6', 0x6, Formatting.GOLD, "\\u001b[33m"),
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
    BLUE('9', 0x9, Formatting.BLUE, "\\u001b[34;1m"),
    /**
     * Represents green
     */
    GREEN('a', 0xA, Formatting.GREEN, "\\u001b[32;1m"),
    /**
     * Represents aqua
     */
    AQUA('b', 0xB, Formatting.AQUA, "\\u001b[36;1m"),
    /**
     * Represents red
     */
    RED('c', 0xC, Formatting.RED, "\\u001b[31;1m"),
    /**
     * Represents light purple
     */
    LIGHT_PURPLE('d', 0xD, Formatting.LIGHT_PURPLE, "\\u001b[35;1m"),
    /**
     * Represents yellow
     */
    YELLOW('e', 0xE, Formatting.YELLOW, "\\u001b[33;1m"),
    /**
     * Represents white
     */
    WHITE('f', 0xF, Formatting.WHITE, "\\u001b[37;1m"),
    /**
     * Represents magical characters that change around randomly
     */
    OBFUSCATED('k', 0x10, Formatting.OBFUSCATED, true),
    /**
     * Makes the text bold.
     */
    BOLD('l', 0x11, Formatting.BOLD, true, "\\u001b[1m"),
    /**
     * Makes a line appear through the text.
     */
    STRIKETHROUGH('m', 0x12, Formatting.STRIKETHROUGH, true),
    /**
     * Makes the text appear underlined.
     */
    UNDERLINE('n', 0x13, Formatting.UNDERLINE, true, "\\u001b[4m"),
    /**
     * Makes the text italic.
     */
    ITALIC('o', 0x14, Formatting.ITALIC, true),
    /**
     * Resets all previous events colors or formats.
     */
    RESET('r', 0x15, Formatting.RESET),
    /**
     * Reverses the Text and background color
     *
     * @apiNote Console Only
     */
    REVERSED('s', 0, null, "\\u001b[7m");

    private static String[] list() {
        return new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "0",
                "a", "b", "c", "d", "e", "f", "i", "k", "l", "m", "o", "r"};
    }

    public static final char COLOR_CHAR = '\u00A7';
    public static final char ALTERNATIVE_COLOR_CHAR = '&';
    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + COLOR_CHAR + "[0-9A-FK-OR]");

    private final int intCode;
    private final char code;
    private final boolean isFormat;
    private final Formatting formatting;
    private final String toString;
    private final String ansi;
    private static final Map<Integer, TextFormat> BY_ID = Maps.newHashMap();
    private static final Map<Character, TextFormat> BY_CHAR = Maps.newHashMap();

    TextFormat(char code, int intCode, Formatting formatting, boolean isFormat, String ansi) {
        this.code = code;
        this.intCode = intCode;
        this.isFormat = isFormat;
        this.formatting = formatting;
        this.toString = new String(new char[]{COLOR_CHAR, code});
        this.ansi = ansi;
    }

    TextFormat(char code, int intCode, Formatting formatting, String ansi) {
        this(code, intCode, formatting, false, ansi);
    }

    TextFormat(char code, int intCode, Formatting formatting) {
        this(code, intCode, formatting, false);
    }

    TextFormat(char code, int intCode, Formatting formatting, boolean isFormat) {
        this(code, intCode, formatting, isFormat, null);
    }


    /**
     * Gets the char value associated with this color
     *
     * @return A char value of this color code
     */
    public char getChar() {
        return code;
    }

    public static String[] getList() {
        return TextFormat.list();
    }

    public Formatting getFormatting() {
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
     * Gets the ANSI code for a TextFormat
     *
     * @return ANSI Formatting code
     */
    public String getAnsiCode() {
        return ansi;
    }

    /**
     * Gets the color represented by the specified color code
     *
     * @param code Code to check
     * @return Associative {@link org.kilocraft.essentials.api} with the given code,
     * or null if it doesn't exist
     */
    @Nullable
    public static TextFormat getByChar(char code) {
        return BY_CHAR.get(code);
    }

    /**
     * Gets the color represented by the specified color code
     *
     * @param code Code to check
     * @return Associative {@link org.kilocraft.essentials.api} with the given code,
     * or null if it doesn't exist
     */
    @Nullable
    public static TextFormat getByChar(@NotNull String code) {
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

    public static Text translateJsonToText(String jsonString) {
        return Text.Serializer.fromJson(jsonString);
    }

    /**
     * Translates a string with legacy style color codes into a Kyori Adventure style.
     *
     * @param altColorChar    The alternate color code character to replace. Ex: {@literal &}
     * @param text Text containing the alternate color code character.
     * @return String containing the new formatting code style.
     */
    public static String translateLegacyToKyoriStyle(final char altColorChar, @NotNull final String text) {
        Validate.notNull(text, "Cannot translate null text");
        String string = text;
        final char[] b = text.toCharArray();
        for (int i = 0; i < b.length; i++) {
            if (b[i] == altColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i + 1]) > -1) {
                final @Nullable TextFormat format = getByChar(b[i + 1]);
                string = string.replace(
                        String.valueOf(altColorChar) + b[i + 1],
                        "<" + (format == null ? "reset" : format.name().toLowerCase(Locale.ENGLISH)) + ">"
                );
            }
        }

        return string;
    }

    /**
     * Translates a string using an alternate color code character into a
     * string that uses the internal ChatColor.COLOR_CODE color code
     * character. The alternate color code character will only be replaced if
     * it is immediately followed by 0-9, A-F, a-f, K-O, k-o, R or r.
     *
     * @param altColorChar    The alternate color code character to replace. Ex: {@literal &}
     * @param textToTranslate Text containing the alternate color code character.
     * @return Text containing the ChatColor.COLOR_CODE color code character.
     */

    @NotNull
    public static String translateAlternateColorCodes(char altColorChar, @NotNull String textToTranslate) {
        Validate.notNull(textToTranslate, "Cannot translate null text");
        char[] b = textToTranslate.toCharArray();

        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == altColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i + 1]) > -1) {
                b[i] = TextFormat.COLOR_CHAR;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }
        return new String(b);
    }

    public static String reverseTranslate(String textToTranslate, char altColorChar) {
        Validate.notNull(textToTranslate, "Cannot translate null text");
        char[] b = textToTranslate.toCharArray();

        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == COLOR_CHAR && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i + 1]) > -1) {
                b[i] = altColorChar;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }
        return new String(b);
    }

    @NotNull
    public static String translate(String string) {
        return translateAlternateColorCodes('&', string);
    }

    @NotNull
    public static String translate(String string, boolean allowFormats) {
        return allowFormats ? translate(string) : TextFormat.removeAlternateColorCodes('&', string);
    }

    public static LiteralText translateToLiteralText(char altColorChar, @NotNull String textToTranslate) {
        return new LiteralText(translateAlternateColorCodes(altColorChar, textToTranslate));
    }

    public static String clearColorCodes(@NotNull String textToClear) {
        return removeAlternateColorCodes(ALTERNATIVE_COLOR_CHAR, textToClear);
    }

    public static String removeAlternateColorCodes(@NotNull String textToTranslate, char... chars) {
        Validate.notNull(textToTranslate, "Cannot translate null text");
        String string = "";
        for (char aChar : chars) {
            string = removeAlternateColorCodes(aChar, string);
        }

        for (LoggerFormats s : LoggerFormats.values()) {
            string = string.replace(s.getCode(), "");
        }

        return string;
    }

    public static String removeAlternateColorCodes(char altColorChar, @NotNull String textToTranslate) {
        Validate.notNull(textToTranslate, "Cannot translate null text");
        for (char c : "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".toCharArray()) {
            textToTranslate = textToTranslate.replace(String.valueOf(TextFormat.COLOR_CHAR) + c, "");
            textToTranslate = textToTranslate.replace(String.valueOf(altColorChar) + c, "");
        }
        return textToTranslate;
    }

    public static LiteralText removeAlternateToLiteralText(char altColorChar, @NotNull String textToTranslate) {
        return new LiteralText(removeAlternateColorCodes(altColorChar, textToTranslate));
    }

    public static void sendToUniversalSource(ServerCommandSource source, String text, boolean log) {
        LiteralText literalText;
        if (CommandUtils.isConsole(source)) {
            literalText = new LiteralText(removeAlternateColorCodes('&', text));
        } else {
            literalText = new LiteralText(translateAlternateColorCodes('&', text));
        }

        source.sendFeedback(literalText, log);
    }

    public static void sendToUniversalSource(char altColorChar, ServerCommandSource source, String text, boolean log) {
        LiteralText literalText;
        if (CommandUtils.isConsole(source)) {
            literalText = new LiteralText(removeAlternateColorCodes(altColorChar, text));
        } else {
            literalText = new LiteralText(translateAlternateColorCodes(altColorChar, text));
        }

        source.sendFeedback(literalText, log);
    }

    public static void sendToUniversalSource(ServerCommandSource source, LiteralText text, boolean log) {
        sendToUniversalSource(source, text.asString(), log);
    }

    public static void sendToSource(ServerCommandSource source, boolean log, String text, Object... objects) {
        sendToUniversalSource(source, String.format(text, objects), log);
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
                TextFormat color = getByChar(c);

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

    public static String getFormattedPing(int i) {
        if (i < 200)
            return "&a" + i;
        if (i > 200 && i < 400)
            return "&e" + i;

        return "&c" + i;
    }

    public static char getFormattedTPS(double tps) {
        if (tps > 15)
            return 'a';
        if (tps > 10)
            return 'e';

        return 'c';
    }

    public static char getFormattedPercentage(double percentage, boolean reverse) {
        return percentage > 80 ? (reverse ? 'c' : 'a') : percentage < 40 ? (reverse ? 'a' : 'c') : 'e';
    }

    public static String getCodeByFormatting(Formatting formatting) {
        for (TextFormat value : values()) {
            if (value.formatting == formatting) {
                return String.valueOf(value.code);
            }
        }

        return null;
    }

    static {
        for (TextFormat color : values()) {
            BY_ID.put(color.intCode, color);
            BY_CHAR.put(color.code, color);
        }
    }
}
