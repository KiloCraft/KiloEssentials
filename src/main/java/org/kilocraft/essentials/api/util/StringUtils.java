package org.kilocraft.essentials.api.util;

import java.util.Locale;
import java.util.regex.Pattern;

public class StringUtils {
    private static final Pattern INVALIDFILECHARS = Pattern.compile("[^a-z0-9-]");

    public static String sanitizeFileName(final String name) {
        return INVALIDFILECHARS.matcher(name.toLowerCase(Locale.ENGLISH)).replaceAll("_");
    }

}
