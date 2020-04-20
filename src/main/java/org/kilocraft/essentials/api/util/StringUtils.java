package org.kilocraft.essentials.api.util;

import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.ModConstants;
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

    /**
     * @author CODY_AI (OnBlock)
     */
    public static class Calculator implements Comparable<Float> {
        private String input;
        private float output;

        public Calculator(@NotNull final String input) {
            this.input = input;
        }

        public void calculate() throws Exception {
            float var = 0.0F;
            String[] strings = this.input.replaceAll("\\s+", "").split(" ");

            this.output = var;
        }

        public float result() {
            return this.output;
        }

        public String resultAsShortString() {
            return ModConstants.DECIMAL_FORMAT.format(this.output);
        }

        @Override
        public int compareTo(@NotNull Float o) {
            return o.compareTo(this.output);
        }
    }
}
