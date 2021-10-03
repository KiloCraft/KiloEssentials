package org.kilocraft.essentials.chat;

import net.kyori.adventure.text.Component;
import net.minecraft.text.LiteralText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.text.ComponentText;

public class StringText {
    public static LiteralText of(@NotNull final String key, @Nullable final Object... objects) {
        return of(true, key, objects);
    }

    public static LiteralText of(final boolean withStyle, @NotNull final String key, @Nullable final Object... objects) {
        final String string = ModConstants.translation(key, objects);
        final Component component = ComponentText.of(string, false);
        return (LiteralText) ComponentText.toText(withStyle ? component : ComponentText.removeStyle(component));
    }

}
