package org.kilocraft.essentials.util;


import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.kilocraft.essentials.api.text.ComponentText;
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

    public static String validatePermission(OnlineUser user, String input, String permissionPrefix) throws CommandSyntaxException {
        for (Format format : Format.values()) {
            String s = input;
            if (!KiloCommands.hasPermission(user.getCommandSource(), permissionPrefix + format.perm, 2)) {
                for (String regex : format.regex) {
                    s = s.toLowerCase().replaceAll(regex, "");
                }
                if (!s.equals(input.toLowerCase())) {
                    throw new SimpleCommandExceptionType(ComponentText.toText(Component.text("You don't have permission to use " + format.toString().toLowerCase()).color(NamedTextColor.RED))).create();
                }
            }
        }
        return input;
    }

}
