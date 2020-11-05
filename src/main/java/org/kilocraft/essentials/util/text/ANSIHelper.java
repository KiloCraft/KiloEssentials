package org.kilocraft.essentials.util.text;

import org.kilocraft.essentials.api.text.LoggerFormats;
import org.kilocraft.essentials.api.text.TextFormat;

import java.util.Objects;


public class ANSIHelper {

    public static String getFormattedMessage(String message) {
        final char[] chars = message.toCharArray();
        final StringBuilder builder = new StringBuilder();
        for (int index = 0; index < chars.length; index++) {
            final char indexed = chars[index];
            if (indexed == TextFormat.COLOR_CHAR) {
                final int codeindex = ++index;
                if (codeindex >= chars.length) {
                    break;
                } else {
                    switch (Objects.requireNonNull(TextFormat.getByChar(chars[codeindex]))) {
                        case BLACK:
                            builder.append(LoggerFormats.BLACK.getCode());
                            break;
                        case DARK_BLUE:
                            builder.append(LoggerFormats.BLUE.getCode());
                            break;
                        case BLUE:
                            builder.append(LoggerFormats.BRIGHT_BLUE.getCode());
                            break;
                        case DARK_GREEN:
                            builder.append(LoggerFormats.GREEN.getCode());
                            break;
                        case GREEN:
                            builder.append(LoggerFormats.BRIGHT_GREEN.getCode());
                            break;
                        case DARK_AQUA:
                            builder.append(LoggerFormats.CYAN.getCode());
                            break;
                        case AQUA:
                            builder.append(LoggerFormats.BRIGHT_CYAN.getCode());
                            break;
                        case DARK_RED:
                            builder.append(LoggerFormats.RED.getCode());
                            break;
                        case RED:
                            builder.append(LoggerFormats.BRIGHT_RED.getCode());
                            break;
                        case DARK_PURPLE:
                            builder.append(LoggerFormats.MAGENTA.getCode());
                            break;
                        case LIGHT_PURPLE:
                            builder.append(LoggerFormats.BRIGHT_MAGENTA.getCode());
                            break;
                        case GOLD:
                            builder.append(LoggerFormats.YELLOW.getCode());
                            break;
                        case YELLOW:
                            builder.append(LoggerFormats.BRIGHT_YELLOW.getCode());
                            break;
                        case DARK_GRAY:
                        case GRAY:
                            builder.append(LoggerFormats.BRIGHT_BLACK.getCode());
                            break;
                        case WHITE:
                            builder.append(LoggerFormats.WHITE.getCode());
                            break;
                        case OBFUSCATED:
                            break;
                        case BOLD:
                            builder.append(LoggerFormats.BOLD.getCode());
                            break;
                        case STRIKETHROUGH:
                            builder.append(LoggerFormats.STRIKETHROUGH.getCode());
                            break;
                        case UNDERLINED:
                            builder.append(LoggerFormats.UNDERLINE.getCode());
                            break;
                        case ITALIC:
                            builder.append(LoggerFormats.ITALICS.getCode());
                            break;
                        case RESET:
                            builder.append(LoggerFormats.RESET.getCode());
                            break;
                        default:
                            builder.append(TextFormat.COLOR_CHAR).append(chars[codeindex]);
                    }
                }
            } else {
                builder.append(indexed);
            }
        }

        return builder + LoggerFormats.RESET.getCode();
    }

}
