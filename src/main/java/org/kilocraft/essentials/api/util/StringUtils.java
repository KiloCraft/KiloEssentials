package org.kilocraft.essentials.api.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.text.TextFormat;

import java.net.SocketAddress;
import java.util.Locale;
import java.util.regex.Pattern;


public class StringUtils {
    public static final String EMPTY_STRING = "";
    private static final Pattern INVALID_FILE_CHARS = Pattern.compile("[^a-z0-9-]");
    public static final Pattern USERNAME_PATTERN = Pattern.compile("/[a-zA-Z][a-zA-Z0-9-_]/gi");
    public static final Pattern UUID_PATTERN = Pattern.compile("([a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8})");

    public static String sanitizeFileName(final String name) {
        return INVALID_FILE_CHARS.matcher(name.toLowerCase(Locale.ENGLISH)).replaceAll("_");
    }

    public static String uniformNickname(final String nickname) {
        return stringToUsername(ComponentText.clearFormatting(TextFormat.clearColorCodes(nickname))).replaceAll("\\s+", "");
    }

    public static String stringToUsername(final String string) {
        return string.replaceAll("[^a-zA-Z0-9_]", "");
    }

    public static String normalizeCapitalization(@NotNull final String string) {
        StringBuilder builder = new StringBuilder();
        String[] strings = string.split(" ");
        int index = 0;
        for (String str : strings) {
            builder.append(
                    String.valueOf(str.charAt(0)).toUpperCase(Locale.ROOT)
            ).append(
                    str.substring(1)
            );

            index++;
            if (index != strings.length) {
                builder.append(" ");
            }
        }

        return builder.toString();
    }

    public static String socketAddressToIp(@NotNull final String address) {
        String string = address;
        string = string.substring(string.indexOf("/") + 1);
        string = string.substring(0, string.indexOf(":"));
        return string;
    }

    public static String socketAddressToPort(@NotNull final String address) {
        String string = address;
        string = string.substring(string.indexOf("/") + 1);
        string = string.substring(string.indexOf(":") + 1);
        return string;
    }

    public static class Calculator implements Comparable<Double> {
        private final String input;
        private double output;

        public Calculator(@NotNull final String input) {
            this.input = input;
        }

        public void calculate() throws Exception {
            String[] strings = this.input.split("(?<=[-+*^%/])|(?=[-+*^%/])");

            for (int i = 0; i < strings.length - 2; i+=2) {
                double x = Double.parseDouble(strings[i]);
                double y = Double.parseDouble(strings[i + 2]);

                Operator operator = Operator.byIcon(strings[i + 1]);
                if (operator == null) {
                    throw new Exception("Invalid Operation!");
                }

                if (i == 0) {
                    this.output = operator.operate(x, y);
                } else {
                    this.output = operator.operate(this.output, y);
                }
            }
        }

        public String getInput() {
            String[] strings = this.input.split("(?<=[-+*^%/])|(?=[-+*^%/])");
            StringBuilder builder = new StringBuilder();

            for (String string : strings) {
                builder.append(string);
            }

            return builder.toString();
        }

        public double result() {
            return this.output;
        }

        public String resultAsShortString() {
            return ModConstants.DECIMAL_FORMAT.format(this.output);
        }

        @Override
        public int compareTo(@NotNull Double o) {
            return o.compareTo(this.output);
        }

        public static String[] operations() {
            String[] strings = new String[Operator.values().length];
            for (int i = 0; i < Operator.values().length; i++) {
                strings[i] = Operator.values()[i].i;
            }

            return strings;
        }

        private enum Operator {
            SUM("addition", "+"),
            MINUS("subtraction", "-"),
            OBELUS("division", "/"),
            TIMES("multiplication", "*"),
            REMAINDER("modules", "%"),
            POWER_OF("power", "^");

            private final String name;
            private final String i;
            Operator(final String name, final String i) {
                this.name = name;
                this.i = i;
            }

            public double operate(final double x, final double y) throws Exception {
                switch (this) {
                    case SUM:
                        return x + y;
                    case MINUS:
                        return x - y;
                    case TIMES:
                        return x * y;
                    case OBELUS:
                        return x / y;
                    case REMAINDER:
                        return x % y;
                    case POWER_OF:
                        return Math.pow(x, y);
                    default:
                        throw new Exception("Invalid Operation!");
                }
            }

            public String getName() {
                return this.name;
            }

            @Nullable
            public static Operator byIcon(final String i) {
                for (Operator value : values()) {
                    if (value.i.equals(i)) {
                        return value;
                    }
                }

                return null;
            }
        }
    }
}
