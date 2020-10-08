package org.kilocraft.essentials.api.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class TextComponent {
    public static Text from(@NotNull final Component component) {
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
}
