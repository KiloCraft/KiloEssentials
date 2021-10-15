package org.kilocraft.essentials.util;


import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.util.commands.KiloCommands;

public enum Format {

    COLOR("color", "<\\/?((color:)?(black|dark_blue|dark_green|dark_aqua|dark_red|dark_purple|gold|gray|dark_gray|blue|green|aqua|red|light_purple|yellow|white))>", "&[a-f0-9]"),
    HEX_COLOR("hex_color", "<\\/?((color:#\\w{6})|(#\\w{6}))>"),
    FORMATTING("formatting", "<\\/?(bold|italic|underlined|obfuscated|strikethrough|reset)>", "&[k-o]"),
    EVENT("event", "<\\/?(((click|insertion)(:[^<>]+)*)|hover:\\w*:('|\")[<>\\w]+('|\"))>"),
    GRADIENT("gradient", "<\\/?gradient(:#?\\w+)*>"),
    RAINBOW("rainbow", "<\\/?rainbow>");

    public final String perm;
    public final String[] regex;

    Format(String perm, String... regex) {
        this.perm = perm;
        this.regex = regex;
    }

    public static String validatePermission(OnlineUser user, String input, String permissionPrefix) {
        for (Format format : Format.values()) {
            if (!KiloEssentials.hasPermissionNode(user.getCommandSource(), permissionPrefix.concat(".").concat(format.perm))) {
                for (String regex : format.regex) {
                    input = input.replaceAll("(?i)" + regex, "");
                }
            }
        }
        return input;
    }

}
