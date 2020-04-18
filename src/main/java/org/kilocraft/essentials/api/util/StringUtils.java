package org.kilocraft.essentials.api.util;

import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.config.KiloConfig;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    private static final Pattern INVALID_FILE_CHARS = Pattern.compile("[^a-z0-9-]");

    public static String sanitizeFileName(final String name) {
        return INVALID_FILE_CHARS.matcher(name.toLowerCase(Locale.ENGLISH)).replaceAll("_");
    }

    public static String censor(@NotNull final String message) {
        String msg = message;
        String lowerCased = msg.toLowerCase(Locale.ROOT);

        for (String value : KiloConfig.messages().censorList().words) {
            String s = value.toLowerCase(Locale.ROOT);
            if (lowerCased.contains(s)) {
                msg = msg.replaceAll(("(?i)" + s), Matcher.quoteReplacement(KiloConfig.messages().censorList().alternateChar));
            }
        }

        return msg;
    }










}
