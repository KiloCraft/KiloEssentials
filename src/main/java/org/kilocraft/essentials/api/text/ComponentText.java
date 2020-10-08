package org.kilocraft.essentials.api.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class ComponentText {
    public static Text empty() {
        return new LiteralText("");
    }

    public static Text toText(@NotNull final Component component) {
        return Text.Serializer.fromJson(GsonComponentSerializer.gson().serialize(component));
    }

    public static Component toComponent(@NotNull final Text text) {
        return GsonComponentSerializer.gson().deserialize(Text.Serializer.toJson(text));
    }

    public static Component of(@NotNull final String raw) {
        return of(raw, true);
    }

    public static Component of(@NotNull final String raw, final boolean markdown) {
        if (markdown) {
            return MiniMessage.markdown().parse(raw);
        }

        return MiniMessage.get().parse(raw);
    }

    public static Component removeEvents(@NotNull final Component component) {
        return component.clickEvent(null).hoverEvent(null);
    }

    public static Component removeStyle(@NotNull final Component component) {
        return component.style(Style.empty());
    }

    public static String formatPercentage(final double percentage) {
        String color;
        if (percentage >= 80.0D) {
            color = NamedTextColor.GREEN.toString();
        } else if (percentage < 40.0D) {
            color = NamedTextColor.YELLOW.toString();
        } else {
            color = NamedTextColor.RED.toString();
        }

        return "<" + color + ">" + percentage;
    }

    public static String formatPing(final double milliseconds) {
        String color;
        if (milliseconds < 200.0D) {
            color = NamedTextColor.GREEN.toString();
        } else if (milliseconds < 400.0D) {
            color = NamedTextColor.YELLOW.toString();
        } else {
            color = NamedTextColor.RED.toString();
        }

        return "<" + color + ">" + milliseconds;
    }

    public static String formatTps(final double tps) {
        String color;
        if (tps > 15.0D) {
            color = NamedTextColor.GREEN.toString();
        } else if (tps > 10) {
            color = NamedTextColor.YELLOW.toString();
        } else {
            color = NamedTextColor.RED.toString();
        }

        return "<" + color + ">" + tps;
    }
}
