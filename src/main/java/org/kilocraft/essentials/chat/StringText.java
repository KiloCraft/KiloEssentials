package org.kilocraft.essentials.chat;

import net.minecraft.text.LiteralText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.text.ComponentText;

public class StringText {

    public static LiteralText of(@NotNull final String key, @Nullable final Object... objects) {
        final String translated = ModConstants.translation(key, objects);
        return (LiteralText) ComponentText.toText(translated);
    }

}
