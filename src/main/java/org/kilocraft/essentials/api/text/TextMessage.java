package org.kilocraft.essentials.api.text;

import net.kyori.adventure.text.Component;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class TextMessage {
    public final String original;
    public final Component component;

    public TextMessage(@NotNull final String raw) {
        this(raw, true);
    }

    public TextMessage(@NotNull final String raw, final boolean markdown) {
        this.original = raw;
        this.component = TextComponent.of(raw, markdown);
    }

    public Text asText() {
        return TextComponent.from(this.component);
    }

    @Override
    public String toString() {
        return this.asText().asString();
    }
}
