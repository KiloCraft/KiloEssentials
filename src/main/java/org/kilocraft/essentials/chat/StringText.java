package org.kilocraft.essentials.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.text.TextFormat;
import org.kilocraft.essentials.commands.CommandUtils;

public class StringText {
    public static LiteralText of(@NotNull final String key, @Nullable final Object... objects) {
        return of(true, key, objects);
    }

    public static LiteralText of(final boolean withStyle, @NotNull final String key, @Nullable final Object... objects) {
        final String string = ModConstants.getStrings().getProperty(key);
        if (string == null) {
            return new LiteralText(key);
        }
        final Component component = ComponentText.of(objects == null ? string : String.format(string, objects));
        return (LiteralText) ComponentText.toText(withStyle ? component : ComponentText.removeStyle(component));
    }

    @Deprecated
    public static void sendToUniversalSource(ServerCommandSource source, String key, boolean log, Object... objects) {
        String result = "";
        String lang = ModConstants.getStrings().getProperty(key);
        if (objects[0] != null) {
            result = String.format(lang, objects);
        }
        LiteralText literalText;
        if (CommandUtils.isConsole(source)) {
            literalText = new LiteralText(TextFormat.removeAlternateColorCodes('&', result));
        } else {
            literalText = new LiteralText(TextFormat.translateAlternateColorCodes('&', result));
        }

        source.sendFeedback(literalText, log);
    }
}
