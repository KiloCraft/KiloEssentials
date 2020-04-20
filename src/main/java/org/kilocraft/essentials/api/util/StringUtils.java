package org.kilocraft.essentials.api.util;

import org.kilocraft.essentials.api.text.TextFormat;

import java.util.Locale;
import java.util.regex.Pattern;

public class StringUtils {
    public static final String EMPTY_STRING = "";
    private static final Pattern INVALID_FILE_CHARS = Pattern.compile("[^a-z0-9-]");
    private static final Pattern USERNAME = Pattern.compile("/[a-zA-Z][a-zA-Z0-9-_]/gi");

    public static String sanitizeFileName(final String name) {
        return INVALID_FILE_CHARS.matcher(name.toLowerCase(Locale.ENGLISH)).replaceAll("_");
    }

    public static String uniformNickname(final String nickname) {
        return stringToUsername(TextFormat.clearColorCodes(nickname)).replaceAll("\\s+", "");
    }

    public static String stringToUsername(final String string) {
        return string.replaceAll("[^A-Za-z0-9()\\\\[\\\\]]", "");
    }

}
